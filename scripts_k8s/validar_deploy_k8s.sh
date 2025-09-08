#!/bin/bash

# Script para validar se o deployment do Tech Challenge está funcionando
# Testa conectividade, APIs e escalabilidade

set -e

echo "🔍 VALIDANDO DEPLOYMENT DO TECH CHALLENGE FASE 2"
echo "================================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
log_success() { echo -e "${GREEN}✅ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}"; }

# Verificar se kubectl está funcionando
if ! kubectl get nodes >/dev/null 2>&1; then
    log_error "kubectl não está funcionando ou cluster não está acessível"
    exit 1
fi

log_success "kubectl funcionando!"
echo ""

# 1. VERIFICAR STATUS DOS PODS
echo "📊 1. VERIFICANDO STATUS DOS PODS"
echo "================================="

pods_status=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --no-headers)
if [ -z "$pods_status" ]; then
    log_error "Nenhum pod encontrado! Execute apply-all.sh primeiro."
    exit 1
fi

echo "$pods_status"
echo ""

# Verificar se todos os pods estão Running
not_running=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --field-selector=status.phase!=Running --no-headers 2>/dev/null || true)
if [ ! -z "$not_running" ]; then
    log_warning "Alguns pods não estão em Running:"
    echo "$not_running"
    echo ""
fi

# 2. VERIFICAR SERVICES
echo "🌐 2. VERIFICANDO SERVICES"
echo "========================="

kubectl get services -l 'app in (autoatendimento,pagamento,mysql)'
echo ""

# 3. VERIFICAR HPA
echo "📈 3. VERIFICANDO HPA (ESCALABILIDADE)"
echo "====================================="

hpa_status=$(kubectl get hpa 2>/dev/null || true)
if [ -z "$hpa_status" ]; then
    log_warning "HPA não encontrado"
else
    echo "$hpa_status"
fi
echo ""

# 4. TESTAR CONECTIVIDADE DOS ENDPOINTS
echo "🔗 4. TESTANDO CONECTIVIDADE DOS ENDPOINTS"
echo "=========================================="

# Função para testar endpoint
test_endpoint() {
    local service=$1
    local port=$2
    local path=$3
    local description=$4
    
    log_info "Testando $description..."
    
    if kubectl run test-curl-$(date +%s) --image=curlimages/curl:latest --rm -i --restart=Never \
        -- curl -f -s "http://$service:$port$path" >/dev/null 2>&1; then
        log_success "$description funcionando!"
        return 0
    else
        log_error "$description não está respondendo!"
        return 1
    fi
}

# Testar endpoints das aplicações
test_endpoint "autoatendimento-service" "8080" "/actuator/health" "Autoatendimento Health Check"
test_endpoint "autoatendimento-service" "8080" "/produtos/categoria/LANCHE" "Autoatendimento API Produtos"
test_endpoint "pagamento-service" "8081" "/actuator/health" "Pagamento Health Check"

echo ""

# 5. TESTAR FLUXO COMPLETO DE PEDIDO
echo "🍔 5. TESTANDO FLUXO COMPLETO DE PEDIDO"
echo "======================================"

log_info "Executando teste de fluxo completo..."

# Criar pod temporário para testes
cat << EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: test-client
spec:
  containers:
  - name: curl
    image: curlimages/curl:latest
    command: ["sleep", "300"]
  restartPolicy: Never
EOF

# Aguardar pod ficar pronto
kubectl wait --for=condition=ready pod/test-client --timeout=60s

# Executar testes dentro do pod
log_info "1. Listando produtos disponíveis..."
if kubectl exec test-client -- curl -s "http://autoatendimento-service:8080/produtos/categoria/LANCHE" >/dev/null; then
    log_success "✅ Lista de produtos funcionando!"
else
    log_error "❌ Falha ao listar produtos"
fi

log_info "2. Testando checkout de pedido..."
PEDIDO_RESPONSE=$(kubectl exec test-client -- curl -s -X POST \
    "http://autoatendimento-service:8080/pedidos/checkout" \
    -H "Content-Type: application/json" \
    -d '{"cpfCliente": null, "itens": [{"produtoId": 1, "quantidade": 1}]}' 2>/dev/null || echo "")

if [ ! -z "$PEDIDO_RESPONSE" ] && echo "$PEDIDO_RESPONSE" | grep -q "id"; then
    log_success "✅ Checkout funcionando!"
    PEDIDO_ID=$(echo "$PEDIDO_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    log_info "ID do pedido criado: $PEDIDO_ID"
    
    # Testar processamento de pagamento
    log_info "3. Testando processamento de pagamento..."
    kubectl exec test-client -- curl -s -X POST \
        "http://pagamento-service:8081/pagamentos" \
        -H "Content-Type: application/json" \
        -d "{\"pedidoId\": \"$PEDIDO_ID\", \"valor\": 18.90}" >/dev/null
    
    log_success "✅ Pagamento enviado para processamento!"
    
    # Aguardar webhook (5 segundos)
    log_info "4. Aguardando webhook automático..."
    sleep 6
    
    # Verificar status do pagamento
    STATUS_RESPONSE=$(kubectl exec test-client -- curl -s \
        "http://autoatendimento-service:8080/pedidos/$PEDIDO_ID/pagamento/status" 2>/dev/null || echo "")
    
    if echo "$STATUS_RESPONSE" | grep -q "APROVADO\|REJEITADO"; then
        log_success "✅ Webhook funcionando! Status: $(echo "$STATUS_RESPONSE" | tr -d '"')"
    else
        log_warning "⚠️  Webhook pode estar lento, status ainda PENDENTE"
    fi
    
else
    log_error "❌ Falha no checkout"
fi

# Limpar pod de teste
kubectl delete pod test-client --ignore-not-found=true >/dev/null 2>&1

echo ""

# 6. TESTAR ESCALABILIDADE (HPA)
echo "📊 6. TESTANDO ESCALABILIDADE (HPA)"
echo "=================================="

if kubectl get hpa >/dev/null 2>&1; then
    log_info "Iniciando teste de carga para verificar escalabilidade..."
    log_warning "Este teste vai durar 2 minutos. Abra outro terminal e execute:"
    log_warning "kubectl get hpa -w"
    log_warning "kubectl get pods -w"
    
    # Criar gerador de carga
    cat << EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: load-generator
spec:
  containers:
  - name: busybox
    image: busybox:latest
    command: ["sh", "-c"]
    args:
    - |
      echo "Iniciando teste de carga..."
      for i in \$(seq 1 120); do
        for j in \$(seq 1 10); do
          wget -q -O- http://autoatendimento-service:8080/produtos/categoria/LANCHE &
        done
        sleep 1
        echo "Segundo \$i/120"
      done
      echo "Teste de carga concluído!"
  restartPolicy: Never
EOF

    log_info "Gerador de carga iniciado! Monitorando por 2 minutos..."
    
    # Monitorar por 30 segundos e mostrar status
    sleep 30
    
    CURRENT_REPLICAS=$(kubectl get deployment autoatendimento-deployment -o jsonpath='{.status.replicas}' 2>/dev/null || echo "unknown")
    log_info "Réplicas atuais do autoatendimento: $CURRENT_REPLICAS"
    
    # Aguardar o teste terminar
    kubectl wait --for=condition=complete pod/load-generator --timeout=150s >/dev/null 2>&1 || true
    
    # Verificar se escalou
    sleep 10
    FINAL_REPLICAS=$(kubectl get deployment autoatendimento-deployment -o jsonpath='{.status.replicas}' 2>/dev/null || echo "unknown")
    
    if [ "$FINAL_REPLICAS" -gt "2" ]; then
        log_success "✅ HPA funcionando! Escalou para $FINAL_REPLICAS réplicas!"
    else
        log_warning "⚠️  HPA pode não ter escalado ainda. Réplicas: $FINAL_REPLICAS"
        log_info "💡 Execute 'kubectl get hpa' para verificar métricas"
    fi
    
    # Limpar gerador de carga
    kubectl delete pod load-generator --ignore-not-found=true >/dev/null 2>&1
    
else
    log_warning "HPA não configurado"
fi

echo ""

# 7. RESUMO FINAL
echo "📋 7. RESUMO DA VALIDAÇÃO"
echo "========================"

# Contar pods rodando
RUNNING_PODS=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --field-selector=status.phase=Running --no-headers | wc -l)
TOTAL_PODS=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --no-headers | wc -l)

log_info "Pods rodando: $RUNNING_PODS/$TOTAL_PODS"

# Verificar services
SERVICES=$(kubectl get services -l 'app in (autoatendimento,pagamento,mysql)' --no-headers | wc -l)
log_info "Services configurados: $SERVICES"

# Verificar HPA
HPA_COUNT=$(kubectl get hpa --no-headers 2>/dev/null | wc -l || echo "0")
log_info "HPAs configurados: $HPA_COUNT"

echo ""
if [ "$RUNNING_PODS" -eq "$TOTAL_PODS" ] && [ "$TOTAL_PODS" -gt "0" ]; then
    log_success "🎉 VALIDAÇÃO CONCLUÍDA COM SUCESSO!"
    log_info "💡 Sistema pronto para demonstração!"
else
    log_warning "⚠️  Alguns componentes podem precisar de ajustes"
    log_info "💡 Execute 'kubectl get pods' para mais detalhes"
fi

echo ""
echo "🌐 URLS DE ACESSO:"
echo "=================="

if command -v minikube >/dev/null 2>&1 && minikube status >/dev/null 2>&1; then
    MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "localhost")
    echo "🍔 Autoatendimento: http://$MINIKUBE_IP:30080"
    echo "🍔 Swagger: http://$MINIKUBE_IP:30080/swagger-ui.html"
    echo "💳 Pagamento: http://$MINIKUBE_IP:30081"
    echo "💳 Swagger: http://$MINIKUBE_IP:30081/swagger-ui.html"
else
    echo "💡 Para acessar via port-forward:"
    echo "kubectl port-forward service/autoatendimento-service 8080:8080"
    echo "kubectl port-forward service/pagamento-service 8081:8081"
fi

echo ""
log_success "Validação concluída! ✨"
