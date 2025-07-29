#!/bin/bash

# Script para aplicar todos os manifests do Tech Challenge Fase 2
# Ordem correta de aplicação para evitar dependências quebradas

set -e

echo "🚀 INICIANDO DEPLOY DO TECH CHALLENGE FASE 2"
echo "============================================="
echo ""

# Verificar se kubectl está funcionando
if ! kubectl get nodes >/dev/null 2>&1; then
    echo "❌ kubectl não está funcionando ou cluster não está acessível"
    echo "💡 Certifique-se de que minikube/kind está rodando e kubectl configurado"
    exit 1
fi

echo "✅ kubectl funcionando!"
echo ""

# Verificar se metrics server está instalado (necessário para HPA)
echo "🔍 Verificando Metrics Server..."
if ! kubectl get deployment metrics-server -n kube-system >/dev/null 2>&1; then
    echo "⚠️  Metrics Server não encontrado!"
    echo "💡 Instalando Metrics Server (necessário para HPA)..."
    kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
    
    # Patch para funcionar no minikube
    kubectl patch deployment metrics-server -n kube-system --type='json' \
        -p='[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--kubelet-insecure-tls"}]'
    
    echo "⏳ Aguardando Metrics Server ficar pronto..."
    kubectl wait --for=condition=available --timeout=300s deployment/metrics-server -n kube-system
fi

echo "✅ Metrics Server disponível!"
echo ""

# Verificar se as imagens Docker existem localmente
echo "🔍 Verificando imagens Docker..."
MISSING_IMAGES=""

# CORRIGIDO: Nomes corretos das imagens com hífen
if ! docker image inspect lanchonete-app-autoatendimento:latest >/dev/null 2>&1; then
    MISSING_IMAGES="$MISSING_IMAGES lanchonete-app-autoatendimento:latest"
fi

if ! docker image inspect lanchonete-app-pagamento:latest >/dev/null 2>&1; then
    MISSING_IMAGES="$MISSING_IMAGES lanchonete-app-pagamento:latest"
fi

if [ ! -z "$MISSING_IMAGES" ]; then
    echo "⚠️  Imagens Docker não encontradas:$MISSING_IMAGES"
    echo "💡 Execute primeiro: docker-compose build"
    echo ""
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✅ Imagens Docker encontradas!"
fi
echo ""

# Função para aguardar pods ficarem prontos
wait_for_pods() {
    local app_name=$1
    local timeout=${2:-300}
    
    echo "⏳ Aguardando pods do $app_name ficarem prontos..."
    
    # Verificar se já existem pods prontos
    local ready_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].status.containerStatuses[*].ready}' 2>/dev/null | grep -o true | wc -l)
    local total_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2>/dev/null | wc -w)
    
    if [ "$ready_pods" -gt 0 ] && [ "$ready_pods" -eq "$total_pods" ]; then
        echo "✅ Pods do $app_name já estão prontos! ($ready_pods/$total_pods)"
        return 0
    fi
    
    # Se não estão prontos, aguardar com timeout
    local elapsed=0
    local check_interval=5
    
    while [ $elapsed -lt $timeout ]; do
        ready_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].status.containerStatuses[*].ready}' 2>/dev/null | grep -o true | wc -l)
        total_pods=$(kubectl get pods -l app=$app_name --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2>/dev/null | wc -w)
        
        if [ "$total_pods" -gt 0 ] && [ "$ready_pods" -eq "$total_pods" ]; then
            echo "✅ Pods do $app_name prontos! ($ready_pods/$total_pods)"
            return 0
        fi
        
        echo "   📊 Status: $ready_pods/$total_pods pods prontos..."
        sleep $check_interval
        elapsed=$((elapsed + check_interval))
    done
    
    # Timeout - mostrar status atual
    echo "⚠️  Timeout aguardando pods do $app_name. Status atual:"
    kubectl get pods -l app=$app_name
    
    # Tentar kubectl wait como fallback (pode funcionar mesmo com timeout)
    echo "🔄 Tentando kubectl wait como fallback..."
    if kubectl wait --for=condition=ready pod -l app=$app_name --timeout=30s >/dev/null 2>&1; then
        echo "✅ Pods do $app_name prontos via kubectl wait!"
        return 0
    fi
    
    echo "❌ Pods do $app_name não ficaram prontos no tempo esperado"
    return 1
}

# Função para verificar se service está respondendo
test_service() {
    local service_name=$1
    local port=$2
    local path=${3:-/actuator/health}
    
    echo "🔍 Testando $service_name..."
    kubectl run test-pod --image=curlimages/curl:latest --rm -i --restart=Never \
        -- curl -f "http://$service_name:$port$path" >/dev/null 2>&1 && \
        echo "✅ $service_name respondendo!" || \
        echo "⚠️  $service_name não está respondendo (normal se ainda iniciando)"
}

echo "📋 FASE 1: CONFIGURAÇÕES E SECRETS"
echo "=================================="

# 1. Criar Secrets primeiro (dependência de tudo)
echo "🔐 Criando Secrets..."
bash k8s/secrets/create-secrets.sh

# 2. Aplicar ConfigMaps
echo "⚙️ Aplicando ConfigMaps..."
kubectl apply -f k8s/configmaps/

echo ""
echo "📋 FASE 2: STORAGE E BANCO DE DADOS"
echo "==================================="

# 3. Aplicar Storage (PVC deve vir antes do StatefulSet)
echo "💾 Configurando storage persistente..."
kubectl apply -f k8s/storage/

# 4. Aplicar MySQL StatefulSet e Services
echo "🐬 Deployando MySQL..."
kubectl apply -f k8s/deployments/mysql-statefulset.yaml
kubectl apply -f k8s/services/mysql-services.yaml

# Aguardar MySQL ficar pronto antes de continuar
wait_for_pods "mysql" 600

echo ""
echo "📋 FASE 3: APLICAÇÕES"
echo "===================="

# 5. Aplicar Deployments das aplicações
echo "🍔 Deployando Autoatendimento..."
kubectl apply -f k8s/deployments/autoatendimento-deployment.yaml

echo "💳 Deployando Pagamento..."
kubectl apply -f k8s/deployments/pagamento-deployment.yaml

# 6. Aplicar Services das aplicações
echo "🌐 Configurando Services..."
kubectl apply -f k8s/services/app-services.yaml

# Aguardar aplicações ficarem prontas
wait_for_pods "autoatendimento" 300
wait_for_pods "pagamento" 300

echo ""
echo "📋 FASE 4: ESCALABILIDADE"
echo "========================"

# 7. Aplicar HPA (só depois que as aplicações estão rodando)
echo "📈 Configurando escalabilidade automática..."
kubectl apply -f k8s/hpa/

echo ""
echo "📋 VERIFICAÇÕES FINAIS"
echo "====================="

# Verificar status geral
echo "📊 Status dos recursos:"
kubectl get pods,services,hpa

echo ""
echo "🔍 Testando conectividade dos serviços..."

# Aguardar um pouco mais para serviços estabilizarem
sleep 30

test_service "autoatendimento-service" "8080"
test_service "pagamento-service" "8081"
test_service "mysql-service" "3306" ""

echo ""
echo "🎯 URLs DE ACESSO (se usando minikube):"
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
echo "🎉 DEPLOY CONCLUÍDO COM SUCESSO!"
echo "================================"
echo ""
echo "📋 PRÓXIMOS PASSOS:"
echo "• Teste os endpoints via Swagger"
echo "• Execute o teste de carga para verificar HPA:"
echo "  kubectl run -i --tty load-generator --rm --image=busybox --restart=Never -- /bin/sh"
echo "  while true; do wget -q -O- http://autoatendimento-service:8080/produtos/categoria/LANCHE; done"
echo "• Monitore a escalabilidade: kubectl get hpa -w"
echo ""
echo "🚀 Tech Challenge Fase 2 deployado com sucesso!"
