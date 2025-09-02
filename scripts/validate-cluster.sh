#!/bin/bash

set -e

echo "✅ VALIDAÇÃO COMPLETA DO CLUSTER EKS"
echo "===================================="

# Função para verificar status e aguardar
wait_for_condition() {
    local description="$1"
    local command="$2"
    local max_attempts=30
    local attempt=1
    
    echo "⏳ $description..."
    
    while [[ $attempt -le $max_attempts ]]; do
        if eval "$command" &>/dev/null; then
            echo "✅ $description - OK"
            return 0
        fi
        
        echo "   Tentativa $attempt/$max_attempts - aguardando..."
        sleep 10
        ((attempt++))
    done
    
    echo "❌ $description - FALHOU após $max_attempts tentativas"
    return 1
}

# 1. Verificar se cluster está ativo
echo "📋 1. VERIFICANDO STATUS DO CLUSTER"
echo "-----------------------------------"

CLUSTER_STATUS=$(aws eks describe-cluster --name lanchonete-cluster --query 'cluster.status' --output text 2>/dev/null || echo "NOTFOUND")

if [[ "$CLUSTER_STATUS" == "ACTIVE" ]]; then
    echo "✅ Cluster está ATIVO"
elif [[ "$CLUSTER_STATUS" == "CREATING" ]]; then
    wait_for_condition "Cluster ficando ativo" "aws eks describe-cluster --name lanchonete-cluster --query 'cluster.status' --output text | grep -q ACTIVE"
elif [[ "$CLUSTER_STATUS" == "NOTFOUND" ]]; then
    echo "❌ Cluster não encontrado"
    exit 1
else
    echo "❌ Status do cluster: $CLUSTER_STATUS"
    exit 1
fi

# 2. Verificar node group
echo ""
echo "📋 2. VERIFICANDO NODE GROUP"
echo "----------------------------"

NODE_GROUP_STATUS=$(aws eks describe-nodegroup --cluster-name lanchonete-cluster --nodegroup-name lanchonete-nodes --query 'nodegroup.status' --output text 2>/dev/null || echo "NOTFOUND")

if [[ "$NODE_GROUP_STATUS" == "ACTIVE" ]]; then
    echo "✅ Node group está ATIVO"
elif [[ "$NODE_GROUP_STATUS" == "CREATING" ]]; then
    wait_for_condition "Node group ficando ativo" "aws eks describe-nodegroup --cluster-name lanchonete-cluster --nodegroup-name lanchonete-nodes --query 'nodegroup.status' --output text | grep -q ACTIVE"
elif [[ "$NODE_GROUP_STATUS" == "NOTFOUND" ]]; then
    echo "❌ Node group não encontrado"
    exit 1
else
    echo "❌ Status do node group: $NODE_GROUP_STATUS"
    exit 1
fi

# 3. Verificar nós via kubectl
echo ""
echo "📋 3. VERIFICANDO NÓS VIA KUBECTL"
echo "---------------------------------"

if ! wait_for_condition "Nós ficando Ready" "kubectl get nodes --no-headers | grep -v NotReady"; then
    echo "❌ Nós não estão Ready"
    kubectl get nodes
    exit 1
fi

# Mostrar informações dos nós
echo ""
echo "🔍 Informações dos nós:"
kubectl get nodes -o wide

# 4. Verificar pods do sistema
echo ""
echo "📋 4. VERIFICANDO PODS DO SISTEMA"
echo "---------------------------------"

if ! wait_for_condition "Pods do sistema rodando" "kubectl get pods -n kube-system --field-selector=status.phase=Running --no-headers | wc -l | grep -E '[5-9]|[1-9][0-9]+'"; then
    echo "❌ Pods do sistema não estão rodando adequadamente"
    kubectl get pods -n kube-system
    exit 1
fi

echo "✅ Pods do sistema estão rodando"

# 5. Verificar ALB
echo ""
echo "📋 5. VERIFICANDO NETWORK LOAD BALANCER"
echo "-------------------------------------------"

ALB_STATUS=$(aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[0].State.Code' --output text 2>/dev/null || echo "NOTFOUND")

if [[ "$ALB_STATUS" == "active" ]]; then
    echo "✅ NLB está ativo"
    
    ALB_DNS=$(aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[0].DNSName' --output text)
    echo "🌐 NLB DNS: $ALB_DNS"
    
elif [[ "$ALB_STATUS" == "provisioning" ]]; then
    wait_for_condition "NLB ficando ativo" "aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[0].State.Code' --output text | grep -q active"
elif [[ "$ALB_STATUS" == "NOTFOUND" ]]; then
    echo "❌ NLB não encontrado"
    exit 1
else
    echo "❌ Status do NLB: $ALB_STATUS"
    exit 1
fi

# 6. Verificar VPC Link
echo ""
echo "📋 6. VERIFICANDO VPC LINK"
echo "-------------------------"

VPC_LINK_STATUS=$(aws apigateway get-vpc-links --query 'items[?name==`lanchonete-eks-vpc-link`].status' --output text 2>/dev/null || echo "NOTFOUND")

if [[ "$VPC_LINK_STATUS" == "AVAILABLE" ]]; then
    echo "✅ VPC Link está disponível"
elif [[ "$VPC_LINK_STATUS" == "PENDING" ]]; then
    wait_for_condition "VPC Link ficando disponível" "aws apigateway get-vpc-links --query 'items[?name==\`lanchonete-eks-vpc-link\`].status' --output text | grep -q AVAILABLE"
elif [[ "$VPC_LINK_STATUS" == "NOTFOUND" ]]; then
    echo "❌ VPC Link não encontrado"
    exit 1
else
    echo "❌ Status do VPC Link: $VPC_LINK_STATUS"
    exit 1
fi

# 7. Teste final de conectividade
echo ""
echo "📋 7. TESTE FINAL DE CONECTIVIDADE"
echo "----------------------------------"

# Testar se conseguimos criar um deployment de teste
TEST_DEPLOYMENT="nginx-test"
kubectl create deployment $TEST_DEPLOYMENT --image=nginx:alpine --replicas=1 2>/dev/null || echo "Deployment já existe"

if wait_for_condition "Pod de teste rodando" "kubectl get pods -l app=$TEST_DEPLOYMENT --field-selector=status.phase=Running --no-headers | wc -l | grep -q 1"; then
    echo "✅ Cluster consegue executar workloads"
    
    # Limpar teste
    kubectl delete deployment $TEST_DEPLOYMENT 2>/dev/null || true
    echo "🧹 Deployment de teste removido"
else
    echo "❌ Cluster não consegue executar workloads"
    kubectl get pods -l app=$TEST_DEPLOYMENT
    kubectl delete deployment $TEST_DEPLOYMENT 2>/dev/null || true
    exit 1
fi

echo ""
echo "🎉 VALIDAÇÃO COMPLETA - SUCESSO!"
echo "================================"
echo "✅ Cluster EKS está totalmente funcional"
echo "✅ Node group com nós Ready"
echo "✅ Pods do sistema rodando"
echo "✅ NLB configurado e ativo"
echo "✅ VPC Link disponível para API Gateway"
echo "✅ Cluster pode executar workloads"
echo ""
echo "🚀 ETAPA 4 CONCLUÍDA COM SUCESSO!"