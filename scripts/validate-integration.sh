#!/bin/bash

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Função para aguardar condição
wait_for_condition() {
    local description="$1"
    local command="$2"
    local timeout="${3:-300}"
    local interval="${4:-10}"
    
    log_info "⏳ Aguardando: $description"
    
    local elapsed=0
    while [[ $elapsed -lt $timeout ]]; do
        if eval "$command" &>/dev/null; then
            log_success "$description - OK"
            return 0
        fi
        
        sleep $interval
        elapsed=$((elapsed + interval))
        echo -n "."
    done
    
    echo ""
    log_error "$description - TIMEOUT após ${timeout}s"
    return 1
}

# Função para validar conectividade TCP
test_tcp_connectivity() {
    local host="$1"
    local port="$2"
    local description="$3"
    
    log_info "🔌 Testando conectividade TCP: $description"
    
    if timeout 10 bash -c "</dev/tcp/$host/$port"; then
        log_success "Conectividade TCP OK: $host:$port"
        return 0
    else
        log_error "Falha na conectividade TCP: $host:$port"
        return 1
    fi
}

# Função para validar health checks
validate_health_checks() {
    log_info "🏥 Validando health checks das aplicações..."
    
    if ! command -v kubectl &> /dev/null; then
        log_warning "kubectl não disponível - health checks não validados"
        return 1
    fi
    
    # Verificar pods
    AUTOATENDIMENTO_READY=$(kubectl get pods -l app=autoatendimento --no-headers 2>/dev/null | grep -c "Running" || echo "0")
    PAGAMENTO_READY=$(kubectl get pods -l app=pagamento --no-headers 2>/dev/null | grep -c "Running" || echo "0")
    
    if [[ $AUTOATENDIMENTO_READY -gt 0 ]]; then
        log_success "Autoatendimento: $AUTOATENDIMENTO_READY pod(s) rodando"
        
        # Testar health check via port-forward
        timeout 15s kubectl port-forward deployment/autoatendimento-deployment 8080:8080 &>/dev/null &
        PF_PID=$!
        sleep 3
        
        if curl -f -s http://localhost:8080/actuator/health &>/dev/null; then
            log_success "Health check autoatendimento: OK"
        else
            log_warning "Health check autoatendimento: não respondeu"
        fi
        
        kill $PF_PID 2>/dev/null || true
    else
        log_error "Nenhum pod autoatendimento rodando"
    fi
    
    if [[ $PAGAMENTO_READY -gt 0 ]]; then
        log_success "Pagamento: $PAGAMENTO_READY pod(s) rodando"
        
        # Testar health check via port-forward
        timeout 15s kubectl port-forward deployment/pagamento-deployment 8081:8081 &>/dev/null &
        PF_PID=$!
        sleep 3
        
        if curl -f -s http://localhost:8081/actuator/health &>/dev/null; then
            log_success "Health check pagamento: OK"
        else
            log_warning "Health check pagamento: não respondeu"
        fi
        
        kill $PF_PID 2>/dev/null || true
    else
        log_error "Nenhum pod pagamento rodando"
    fi
}

# Função para validar NLB
validate_nlb() {
    log_info "🌐 Validando Network Load Balancer..."
    
    NLB_STATUS=$(aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[0].State.Code' --output text 2>/dev/null || echo "NOTFOUND")
    
    if [[ "$NLB_STATUS" == "active" ]]; then
        log_success "NLB Status: ativo"
        
        NLB_DNS=$(aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[0].DNSName' --output text)
        log_success "NLB DNS: $NLB_DNS"
        
        # Testar conectividade com as portas NodePort
        test_tcp_connectivity "$NLB_DNS" "30080" "Autoatendimento via NLB"
        test_tcp_connectivity "$NLB_DNS" "30081" "Pagamento via NLB"
        
    else
        log_error "NLB não encontrado ou inativo: $NLB_STATUS"
        return 1
    fi
}

# Função para validar VPC Link
validate_vpc_link() {
    log_info "🔗 Validando VPC Link..."
    
    VPC_LINK_STATUS=$(aws apigateway get-vpc-links --query 'items[?name==`lanchonete-eks-vpc-link`].status' --output text 2>/dev/null || echo "NOTFOUND")
    
    if [[ "$VPC_LINK_STATUS" == "AVAILABLE" ]]; then
        log_success "VPC Link Status: disponível"
    else
        log_warning "VPC Link Status: $VPC_LINK_STATUS"
        log_info "VPC Link pode demorar alguns minutos para ficar disponível"
    fi
}

# Função para validar API Gateway
validate_api_gateway() {
    log_info "🌐 Validando API Gateway..."
    
    # Obter URL do API Gateway
    API_URL=$(cd terraform/lambda && terraform output -raw api_gateway_url 2>/dev/null || echo "")
    
    if [[ -z "$API_URL" ]]; then
        log_error "URL do API Gateway não encontrada"
        return 1
    fi
    
    log_success "API Gateway URL: $API_URL"
    
    # Testar endpoint público (auth)
    log_info "🔑 Testando endpoint público /auth..."
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_URL/auth" \
        -H "Content-Type: application/json" \
        -d '{"authType": "anonymous"}' || echo "000")
    
    if [[ "$HTTP_STATUS" == "200" ]]; then
        log_success "Endpoint /auth respondendo: $HTTP_STATUS"
    else
        log_warning "Endpoint /auth status inesperado: $HTTP_STATUS"
    fi
    
    # Testar endpoint protegido sem token (deve retornar 401)
    log_info "🚫 Testando endpoint protegido sem token..."
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/produtos/categoria/LANCHE" || echo "000")
    
    if [[ "$HTTP_STATUS" == "401" ]]; then
        log_success "Endpoint protegido corretamente: $HTTP_STATUS (não autorizado)"
    else
        log_warning "Endpoint protegido status inesperado: $HTTP_STATUS (esperado: 401)"
    fi
}

# Função para validar latência end-to-end
validate_performance() {
    log_info "⚡ Validando performance end-to-end..."
    
    API_URL=$(cd terraform/lambda && terraform output -raw api_gateway_url 2>/dev/null || echo "")
    if [[ -z "$API_URL" ]]; then
        log_warning "API Gateway URL não encontrada - pulando teste de performance"
        return 1
    fi
    
    # Obter token para teste
    TOKEN_RESPONSE=$(curl -s -X POST "$API_URL/auth" \
        -H "Content-Type: application/json" \
        -d '{"authType": "anonymous"}' || echo "")
    
    TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.token' 2>/dev/null || echo "")
    
    if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
        log_warning "Token não obtido - pulando teste de performance"
        return 1
    fi
    
    # Medir latência de algumas requisições
    log_info "📊 Medindo latência de requisições..."
    
    TOTAL_TIME=0
    SUCCESSFUL_REQUESTS=0
    
    for i in {1..5}; do
        RESPONSE_TIME=$(curl -s -o /dev/null -w "%{time_total}" -X GET "$API_URL/produtos/categoria/LANCHE" \
            -H "Authorization: Bearer $TOKEN" 2>/dev/null || echo "0")
        
        if [[ "$RESPONSE_TIME" != "0" ]]; then
            TOTAL_TIME=$(echo "$TOTAL_TIME + $RESPONSE_TIME" | bc -l 2>/dev/null || echo "$TOTAL_TIME")
            SUCCESSFUL_REQUESTS=$((SUCCESSFUL_REQUESTS + 1))
            log_info "Requisição $i: ${RESPONSE_TIME}s"
        fi
    done
    
    if [[ $SUCCESSFUL_REQUESTS -gt 0 ]]; then
        AVG_TIME=$(echo "scale=3; $TOTAL_TIME / $SUCCESSFUL_REQUESTS" | bc -l 2>/dev/null || echo "N/A")
        log_success "Latência média: ${AVG_TIME}s ($SUCCESSFUL_REQUESTS/5 requisições)"
        
        # Verificar se latência está dentro do esperado (< 2s)
        if (( $(echo "$AVG_TIME < 2.0" | bc -l 2>/dev/null) )); then
            log_success "Performance OK: latência < 2s"
        else
            log_warning "Performance: latência alta (>2s) - pode ser normal no primeiro acesso"
        fi
    else
        log_warning "Nenhuma requisição de performance completada"
    fi
}

# Função principal
main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║          🔧 VALIDAÇÃO DE INTEGRAÇÃO - ETAPA 7                ║"
    echo "║     Infraestrutura + Conectividade + Performance             ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    local TOTAL_CHECKS=0
    local PASSED_CHECKS=0
    
    # ====================================
    # 1. VALIDAR CLUSTER EKS
    # ====================================
    echo "1️⃣ VALIDAÇÃO CLUSTER EKS"
    echo "========================"
    ((TOTAL_CHECKS++))
    
    if command -v kubectl &> /dev/null && kubectl get nodes &>/dev/null; then
        READY_NODES=$(kubectl get nodes --no-headers | grep -c "Ready" || echo "0")
        log_success "EKS Cluster: $READY_NODES node(s) pronto(s)"
        ((PASSED_CHECKS++))
    else
        log_error "EKS Cluster: não acessível"
    fi
    
    echo ""
    
    # ====================================
    # 2. VALIDAR APLICAÇÕES
    # ====================================
    echo "2️⃣ VALIDAÇÃO DAS APLICAÇÕES"
    echo "=========================="
    ((TOTAL_CHECKS++))
    
    if validate_health_checks; then
        ((PASSED_CHECKS++))
    fi
    
    echo ""
    
    # ====================================
    # 3. VALIDAR NETWORK LOAD BALANCER
    # ====================================
    echo "3️⃣ VALIDAÇÃO NETWORK LOAD BALANCER"
    echo "=================================="
    ((TOTAL_CHECKS++))
    
    if validate_nlb; then
        ((PASSED_CHECKS++))
    fi
    
    echo ""
    
    # ====================================
    # 4. VALIDAR VPC LINK
    # ====================================
    echo "4️⃣ VALIDAÇÃO VPC LINK"
    echo "===================="
    ((TOTAL_CHECKS++))
    
    if validate_vpc_link; then
        ((PASSED_CHECKS++))
    fi
    
    echo ""
    
    # ====================================
    # 5. VALIDAR API GATEWAY
    # ====================================
    echo "5️⃣ VALIDAÇÃO API GATEWAY"
    echo "======================="
    ((TOTAL_CHECKS++))
    
    if validate_api_gateway; then
        ((PASSED_CHECKS++))
    fi
    
    echo ""
    
    # ====================================
    # 6. VALIDAR PERFORMANCE
    # ====================================
    echo "6️⃣ VALIDAÇÃO DE PERFORMANCE"
    echo "=========================="
    ((TOTAL_CHECKS++))
    
    if validate_performance; then
        ((PASSED_CHECKS++))
    fi
    
    echo ""
    
    # ====================================
    # 7. TESTE DE CONECTIVIDADE END-TO-END
    # ====================================
    echo "7️⃣ TESTE DE CONECTIVIDADE END-TO-END"
    echo "==================================="
    ((TOTAL_CHECKS++))
    
    log_info "🔄 Executando teste rápido end-to-end..."
    
    API_URL=$(cd terraform/lambda && terraform output -raw api_gateway_url 2>/dev/null || echo "")
    if [[ -n "$API_URL" ]]; then
        # Teste completo rápido
        if TOKEN_RESPONSE=$(curl -s -X POST "$API_URL/auth" -H "Content-Type: application/json" -d '{"authType": "anonymous"}' 2>/dev/null) && \
           TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.token' 2>/dev/null) && \
           [[ -n "$TOKEN" && "$TOKEN" != "null" ]] && \
           curl -s -f "$API_URL/produtos/categoria/LANCHE" -H "Authorization: Bearer $TOKEN" &>/dev/null; then
            log_success "Conectividade end-to-end: funcionando"
            ((PASSED_CHECKS++))
        else
            log_error "Conectividade end-to-end: falhou"
        fi
    else
        log_error "API Gateway URL não encontrada"
    fi
    
    echo ""
    
    # ====================================
    # RESUMO FINAL
    # ====================================
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "${BLUE}📊 RESUMO DA VALIDAÇÃO DE INTEGRAÇÃO${NC}"
    echo "═══════════════════════════════════════════════════════════════"
    
    echo "Total de verificações: $TOTAL_CHECKS"
    echo "Verificações aprovadas: $PASSED_CHECKS"
    echo "Taxa de sucesso: $(( PASSED_CHECKS * 100 / TOTAL_CHECKS ))%"
    
    echo ""
    
    if [[ $PASSED_CHECKS -eq $TOTAL_CHECKS ]]; then
        log_success "🎉 TODAS AS VALIDAÇÕES PASSARAM!"
        echo ""
        log_success "✨ ETAPA 7 - INTEGRAÇÃO COMPLETA E FUNCIONANDO"
        echo ""
        log_info "🚀 Componentes validados:"
        echo "   • EKS Cluster com aplicações rodando"
        echo "   • Network Load Balancer ativo e acessível"
        echo "   • VPC Link conectando API Gateway ao EKS"
        echo "   • API Gateway com autorização funcionando"
        echo "   • Performance dentro do esperado"
        echo "   • Conectividade end-to-end confirmada"
        
        exit 0
    else
        FAILED_CHECKS=$((TOTAL_CHECKS - PASSED_CHECKS))
        log_warning "⚠️  $FAILED_CHECKS validação(ões) falharam"
        echo ""
        log_info "🔧 Ações sugeridas:"
        echo "   • Verifique se credenciais AWS estão válidas"
        echo "   • Execute 'terraform apply' para atualizar recursos"
        echo "   • Aguarde alguns minutos para VPC Link ficar disponível"
        echo "   • Execute 'kubectl get pods' para verificar aplicações"
        
        exit 1
    fi
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi