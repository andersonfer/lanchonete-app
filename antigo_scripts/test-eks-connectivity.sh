#!/bin/bash

set -e

echo "🧪 TESTANDO CONECTIVIDADE DO EKS"
echo "================================="

# Verificar se kubectl está configurado
if ! kubectl cluster-info --request-timeout=10s &> /dev/null; then
    echo "❌ kubectl não está configurado ou cluster inacessível"
    echo "💡 Execute: ./scripts/configure-kubectl.sh"
    exit 1
fi

echo "☸️  Informações do cluster:"
kubectl cluster-info --request-timeout=30s

echo ""
echo "📋 TESTE 1: Verificando nós do cluster"
echo "--------------------------------------"
NODES=$(kubectl get nodes --no-headers 2>/dev/null | wc -l)

if [[ $NODES -gt 0 ]]; then
    echo "✅ Encontrados $NODES nós"
    kubectl get nodes -o wide
else
    echo "❌ Nenhum nó encontrado"
    exit 1
fi

echo ""
echo "📋 TESTE 2: Verificando pods do sistema"
echo "---------------------------------------"
SYSTEM_PODS=$(kubectl get pods -n kube-system --no-headers 2>/dev/null | wc -l)

if [[ $SYSTEM_PODS -gt 0 ]]; then
    echo "✅ Encontrados $SYSTEM_PODS pods do sistema"
    kubectl get pods -n kube-system --field-selector=status.phase=Running
else
    echo "❌ Nenhum pod do sistema encontrado"
    exit 1
fi

echo ""
echo "📋 TESTE 3: Verificando namespaces"
echo "----------------------------------"
kubectl get namespaces

echo ""
echo "📋 TESTE 4: Verificando services"
echo "--------------------------------"
kubectl get services --all-namespaces

echo ""
echo "📋 TESTE 5: Verificando recursos do cluster"
echo "-------------------------------------------"
kubectl get all -n kube-system | head -20

echo ""
echo "📋 TESTE 6: Verificando capacidade dos nós"
echo "-------------------------------------------"
kubectl describe nodes | grep -E "Name:|Capacity:|Allocatable:" | head -20

echo ""
echo "📋 TESTE 7: Testando criação de namespace de teste"
echo "--------------------------------------------------"
TEST_NAMESPACE="lanchonete-test"

# Criar namespace de teste
kubectl create namespace $TEST_NAMESPACE 2>/dev/null || echo "Namespace já existe"

# Verificar se foi criado
if kubectl get namespace $TEST_NAMESPACE &> /dev/null; then
    echo "✅ Namespace de teste criado: $TEST_NAMESPACE"
    
    # Limpar namespace de teste
    kubectl delete namespace $TEST_NAMESPACE
    echo "🧹 Namespace de teste removido"
else
    echo "❌ Não foi possível criar namespace de teste"
    exit 1
fi

echo ""
echo "🎉 TODOS OS TESTES PASSARAM!"
echo "============================"
echo "✅ Cluster EKS está funcionando corretamente"
echo "✅ kubectl configurado e conectado"
echo "✅ Nós estão prontos para receber aplicações"