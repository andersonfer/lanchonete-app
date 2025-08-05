#!/bin/bash

# Script automatizado para deploy do Tech Challenge Fase 2 no Kubernetes
# Atualizado para suportar secrets baseados em variáveis de ambiente

set -e

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Funções de log
log_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
log_success() { echo -e "${GREEN}✅ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
log_error() { echo -e "${RED}❌ $1${NC}"; }

# Função para aguardar pods ficarem prontos
wait_for_pods() {
    local app_name=$1
    local timeout=${2:-300}
    
    log_info "Aguardando pods do $app_name ficarem prontos..."
    
    # Verificação rápida inicial
    ready_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].status.containerStatuses[*].ready}' 2>/dev/null | grep -o true | wc -l)
    total_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2>/dev/null | wc -w)
    
    if [ "$total_pods" -gt 0 ] && [ "$ready_pods" -eq "$total_pods" ]; then
        log_success "Pods do $app_name já estão prontos! ($ready_pods/$total_pods)"
        return 0
    fi
    
    # Se não estão prontos, aguardar com timeout
    local elapsed=0
    local check_interval=5
    
    while [ $elapsed -lt $timeout ]; do
        ready_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].status.containerStatuses[*].ready}' 2>/dev/null | grep -o true | wc -l)
        total_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2>/dev/null | wc -w)
        
        if [ "$total_pods" -gt 0 ] && [ "$ready_pods" -eq "$total_pods" ]; then
            log_success "Pods do $app_name prontos! ($ready_pods/$total_pods)"
            return 0
        fi
        
        echo "   📊 Status: $ready_pods/$total_pods pods prontos..."
        sleep $check_interval
        elapsed=$((elapsed + check_interval))
    done
    
    # Timeout - mostrar status atual
    log_warning "Timeout aguardando pods do $app_name. Status atual:"
    kubectl get pods -l app=$app_name
    
    # Tentar kubectl wait como fallback (pode funcionar mesmo com timeout)
    log_info "Tentando kubectl wait como fallback..."
    if kubectl wait --for=condition=ready pod -l app=$app_name --timeout=30s >/dev/null 2>&1; then
        log_success "Pods do $app_name prontos via kubectl wait!"
        return 0
    fi
    
    log_error "Pods do $app_name não ficaram prontos no tempo esperado"
    return 1
}

# Função para verificar se service está respondendo
test_service() {
    local service_name=$1
    local port=$2
    local path=${3:-/actuator/health}
    
    log_info "Testando $service_name..."
    kubectl run test-pod --image=curlimages/curl:latest --rm -i --restart=Never \
        -- curl -f "http://$service_name:$port$path" >/dev/null 2>&1 && \
        log_success "$service_name respondendo!" || \
        log_warning "$service_name não está respondendo (normal se ainda iniciando)"
}

# Função para verificar variáveis de ambiente necessárias
check_environment_variables() {
    log_info "Verificando variáveis de ambiente para secrets..."
    
    local missing_vars=()
    
    if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
        missing_vars+=("MYSQL_ROOT_PASSWORD")
    fi
    
    if [ -z "$MYSQL_USER_PASSWORD" ]; then
        missing_vars+=("MYSQL_USER_PASSWORD")
    fi
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        log_error "Variáveis de ambiente obrigatórias não estão definidas:"
        for var in "${missing_vars[@]}"; do
            echo "  - $var"
        done
        echo ""
        log_warning "Para resolver:"
        echo "Use o arquivo .env:"
        echo "   cp .env.example .env"
        echo "   export \$(cat .env | grep -v '^#' | xargs)"
        echo ""
        return 1
    fi
    
    log_success "Variáveis de ambiente configuradas!"
    return 0
}

echo ""
log_info "🚀 INICIANDO DEPLOY DO TECH CHALLENGE FASE 2"
echo "============================================="
echo ""

# Verificar pré-requisitos
if ! kubectl get nodes >/dev/null 2>&1; then
    log_error "kubectl não está funcionando ou cluster não está acessível"
    exit 1
fi

log_success "kubectl funcionando!"

echo ""
log_info "📋 FASE 1: CONFIGURAÇÕES E SECRETS"
echo "=================================="

# Verificar variáveis de ambiente antes de tentar criar secrets
if ! check_environment_variables; then
    exit 1
fi

# 1. Criar Secrets primeiro (dependência de tudo)
log_info "Criando Secrets..."
bash k8s/secrets/create_secrets.sh

# 2. Aplicar ConfigMaps
log_info "Aplicando ConfigMaps..."
kubectl apply -f k8s/configmaps/

echo ""
log_info "📋 FASE 2: STORAGE E BANCO DE DADOS"
echo "==================================="

# 3. Aplicar Storage (PVC deve vir antes do StatefulSet)
log_info "Configurando storage persistente..."
kubectl apply -f k8s/storage/

# 4. Aplicar MySQL StatefulSet e Services
log_info "Deployando MySQL..."
kubectl apply -f k8s/deployments/mysql-statefulset.yaml
kubectl apply -f k8s/services/mysql-services.yaml

# Aguardar MySQL ficar pronto antes de continuar
wait_for_pods "mysql" 600

echo ""
log_info "📋 FASE 3: APLICAÇÕES"
echo "===================="

# 5. Aplicar Deployments das aplicações
log_info "Deployando Autoatendimento..."
kubectl apply -f k8s/deployments/autoatendimento-deployment.yaml

log_info "Deployando Pagamento..."
kubectl apply -f k8s/deployments/pagamento-deployment.yaml

# 6. Aplicar Services das aplicações
log_info "Configurando Services..."
kubectl apply -f k8s/services/app-services.yaml

# Aguardar aplicações ficarem prontas
wait_for_pods "autoatendimento" 300
wait_for_pods "pagamento" 300

echo ""
log_info "📋 FASE 4: ESCALABILIDADE"
echo "========================"

# 7. Aplicar HPA (só depois que as aplicações estão rodando)
log_info "Configurando escalabilidade automática..."
kubectl apply -f k8s/hpa/

echo ""
log_info "📋 VERIFICAÇÕES FINAIS"
echo "====================="

# Verificar status geral
log_info "Status dos recursos:"
kubectl get pods,services,hpa

echo ""
log_info "Testando conectividade dos serviços..."

# Aguardar um pouco mais para serviços estabilizarem
sleep 30

test_service "autoatendimento-service" "8080"
test_service "pagamento-service" "8081"
test_service "mysql-service" "3306" ""

echo ""
log_info "🎯 URLs DE ACESSO (se usando minikube):"
echo "======================================="

if command -v minikube >/dev/null 2>&1 && minikube status >/dev/null 2>&1; then
    MINIKUBE_IP=$(minikube ip)
    echo "🍔 Autoatendimento: http://$MINIKUBE_IP:30080"
    echo "🍔 Swagger Autoatendimento: http://$MINIKUBE_IP:30080/swagger-ui.html"
    echo "💳 Pagamento: http://$MINIKUBE_IP:30081"
    echo "💳 Swagger Pagamento: http://$MINIKUBE_IP:30081/swagger-ui.html"
else
    echo "💡 Para acessar externamente:"
    echo "   kubectl port-forward service/autoatendimento-service 8080:8080"
    echo "   kubectl port-forward service/pagamento-service 8081:8081"
fi

echo ""
log_success "🎉 DEPLOY CONCLUÍDO COM SUCESSO!"
echo "================================"
echo ""
log_info "📋 PRÓXIMOS PASSOS:"
echo "• Teste os endpoints via Swagger"
echo "• Execute o teste de carga para verificar HPA:"
echo "  kubectl run -i --tty load-generator --rm --image=busybox --restart=Never -- /bin/sh"
echo "  while true; do wget -q -O- http://autoatendimento-service:8080/produtos/categoria/LANCHE; done"
echo "• Monitore a escalabilidade: kubectl get hpa -w"
echo ""
log_success "🚀 Tech Challenge Fase 2 deployado com sucesso!"
