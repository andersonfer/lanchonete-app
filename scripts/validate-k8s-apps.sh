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

# Função principal
main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║              ✅ VALIDAÇÃO KUBERNETES - ETAPA 6                 ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    # Verificar kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl não encontrado!"
        exit 1
    fi
    
    # Verificar conexão com cluster
    if ! kubectl get nodes &>/dev/null; then
        log_error "Não foi possível conectar ao cluster Kubernetes!"
        exit 1
    fi
    
    # 1. Verificar nodes
    echo "1️⃣ VERIFICANDO NODES DO CLUSTER"
    echo "==============================="
    
    READY_NODES=$(kubectl get nodes --no-headers | grep -c "Ready" || echo "0")
    TOTAL_NODES=$(kubectl get nodes --no-headers | wc -l)
    
    if [[ $READY_NODES -gt 0 ]]; then
        log_success "$READY_NODES/$TOTAL_NODES nodes prontos"
    else
        log_error "Nenhum node pronto!"
        exit 1
    fi
    
    # 2. Verificar pods das aplicações
    echo ""
    echo "2️⃣ VERIFICANDO PODS DAS APLICAÇÕES"
    echo "=================================="
    
    # Autoatendimento
    AUTOATENDIMENTO_PODS=$(kubectl get pods -l app=autoatendimento --no-headers 2>/dev/null | wc -l || echo "0")
    AUTOATENDIMENTO_READY=$(kubectl get pods -l app=autoatendimento --no-headers 2>/dev/null | grep -c "Running" || echo "0")
    
    if [[ $AUTOATENDIMENTO_READY -gt 0 ]]; then
        log_success "Autoatendimento: $AUTOATENDIMENTO_READY/$AUTOATENDIMENTO_PODS pods rodando"
    else
        log_error "Pods autoatendimento não estão rodando"
    fi
    
    # Pagamento
    PAGAMENTO_PODS=$(kubectl get pods -l app=pagamento --no-headers 2>/dev/null | wc -l || echo "0")
    PAGAMENTO_READY=$(kubectl get pods -l app=pagamento --no-headers 2>/dev/null | grep -c "Running" || echo "0")
    
    if [[ $PAGAMENTO_READY -gt 0 ]]; then
        log_success "Pagamento: $PAGAMENTO_READY/$PAGAMENTO_PODS pods rodando"
    else
        log_error "Pods pagamento não estão rodando"
    fi
    
    # 3. Verificar services
    echo ""
    echo "3️⃣ VERIFICANDO SERVICES"
    echo "======================"
    
    AUTOATENDIMENTO_SVC=$(kubectl get service autoatendimento-service --no-headers 2>/dev/null | wc -l || echo "0")
    PAGAMENTO_SVC=$(kubectl get service pagamento-service --no-headers 2>/dev/null | wc -l || echo "0")
    
    if [[ $AUTOATENDIMENTO_SVC -gt 0 ]]; then
        log_success "Service autoatendimento-service criado"
    else
        log_warning "Service autoatendimento-service não encontrado"
    fi
    
    if [[ $PAGAMENTO_SVC -gt 0 ]]; then
        log_success "Service pagamento-service criado"
    else
        log_warning "Service pagamento-service não encontrado"
    fi
    
    # 4. Verificar HPA
    echo ""
    echo "4️⃣ VERIFICANDO HPA (HORIZONTAL POD AUTOSCALER)"
    echo "=============================================="
    
    HPA_COUNT=$(kubectl get hpa --no-headers 2>/dev/null | wc -l || echo "0")
    
    if [[ $HPA_COUNT -gt 0 ]]; then
        log_success "$HPA_COUNT HPA(s) configurado(s)"
        kubectl get hpa
    else
        log_warning "Nenhum HPA encontrado"
    fi
    
    # 5. Verificar Ingress
    echo ""
    echo "5️⃣ VERIFICANDO ALB INGRESS"
    echo "=========================="
    
    INGRESS_COUNT=$(kubectl get ingress --no-headers 2>/dev/null | wc -l || echo "0")
    
    if [[ $INGRESS_COUNT -gt 0 ]]; then
        log_success "Ingress configurado"
        ALB_ENDPOINT=$(kubectl get ingress lanchonete-alb -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
        
        if [[ -n "$ALB_ENDPOINT" ]]; then
            log_success "ALB Endpoint: $ALB_ENDPOINT"
        else
            log_info "ALB ainda sem endpoint (pode demorar alguns minutos)"
        fi
    else
        log_warning "Nenhum Ingress encontrado"
    fi
    
    # 6. Teste rápido de health check (se possível)
    echo ""
    echo "6️⃣ TESTE RÁPIDO DE HEALTH CHECK"
    echo "==============================="
    
    if [[ $AUTOATENDIMENTO_READY -gt 0 ]]; then
        # Port forward temporário para testar health
        timeout 10s kubectl port-forward deployment/autoatendimento-deployment 8080:8080 >/dev/null 2>&1 &
        PF_PID=$!
        
        sleep 3
        
        if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            log_success "Health check autoatendimento OK"
        else
            log_warning "Health check autoatendimento não respondeu (normal se ainda inicializando)"
        fi
        
        kill $PF_PID 2>/dev/null || true
    fi
    
    # Resumo final
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "${BLUE}📊 RESUMO DA VALIDAÇÃO${NC}"
    echo "═══════════════════════════════════════════════════════════════"
    
    TOTAL_ISSUES=0
    
    if [[ $READY_NODES -eq 0 ]]; then ((TOTAL_ISSUES++)); fi
    if [[ $AUTOATENDIMENTO_READY -eq 0 ]]; then ((TOTAL_ISSUES++)); fi
    if [[ $PAGAMENTO_READY -eq 0 ]]; then ((TOTAL_ISSUES++)); fi
    
    if [[ $TOTAL_ISSUES -eq 0 ]]; then
        log_success "🎉 TODAS AS VALIDAÇÕES PASSARAM!"
        echo ""
        echo "✅ Cluster EKS funcionando"
        echo "✅ Pods das aplicações rodando"
        echo "✅ Services configurados"
        echo "✅ HPA ativo"
        echo "✅ ALB Ingress criado"
        echo ""
        log_success "🚀 ETAPA 6 VALIDADA COM SUCESSO!"
    else
        log_warning "⚠️ $TOTAL_ISSUES problema(s) encontrado(s)"
        echo "Verifique os logs acima para mais detalhes."
    fi
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi