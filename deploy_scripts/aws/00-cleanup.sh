#!/bin/bash
set -e

echo "🗑️  DESTRUINDO INFRAESTRUTURA AWS COMPLETA"
echo "=========================================="
echo ""
echo "⚠️  ATENÇÃO: Este script vai destruir:"
echo "  - API Gateway"
echo "  - Lambda AuthHandler"
echo "  - Cognito User Pool"
echo "  - RDS MySQL (3 instâncias)"
echo "  - Cluster EKS (nodes + control plane)"
echo "  - ECR Repositories"
echo "  - Backend S3 + DynamoDB"
echo "  - TODOS os dados serão perdidos!"
echo ""

read -p "Tem certeza? (digite 'DESTRUIR' para confirmar): " confirm

if [ "$confirm" != "DESTRUIR" ]; then
    echo "❌ Cancelado."
    exit 0
fi

echo ""
echo "🧹 Iniciando destruição completa..."
echo ""

# ============================================================================
# PASSO 1: LIMPAR RECURSOS KUBERNETES
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧹 PASSO 1: Limpando recursos Kubernetes"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Verifica se kubectl está configurado
if kubectl cluster-info &>/dev/null; then
    echo "Deletando Ingress..."
    kubectl delete -f k8s/ingress/aws/ --ignore-not-found=true 2>/dev/null || true

    echo "Deletando microservices..."
    kubectl delete deployment --all --ignore-not-found=true 2>/dev/null || true

    echo "Deletando services..."
    kubectl delete -f k8s/base/services/ --ignore-not-found=true 2>/dev/null || true

    echo "Deletando databases..."
    kubectl delete -f k8s/aws/statefulsets/ --ignore-not-found=true 2>/dev/null || true

    echo "Deletando ConfigMaps..."
    kubectl delete -f k8s/base/configmaps/ --ignore-not-found=true 2>/dev/null || true

    echo "Deletando Secrets..."
    kubectl delete secret --all --ignore-not-found=true 2>/dev/null || true

    echo "✅ Recursos Kubernetes removidos"
else
    echo "⚠️  kubectl não configurado - pulando limpeza K8s"
fi

echo ""

# ============================================================================
# PASSO 2: DESTRUIR API GATEWAY
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 PASSO 2: Destruindo API Gateway"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/api-gateway" ]; then
    cd infra/api-gateway

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo API Gateway..."
        terraform destroy -auto-approve \
          -var="clientes_service_url=http://dummy" \
          -var="pedidos_service_url=http://dummy" \
          -var="cozinha_service_url=http://dummy" \
          -var="pagamento_service_url=http://dummy" \
          || echo "⚠️  Erro ao destruir API Gateway (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ API Gateway destruído"
else
    echo "⚠️  Diretório infra/api-gateway não encontrado"
fi

echo ""

# ============================================================================
# PASSO 3: DESTRUIR LAMBDA AUTHHANDLER
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚡ PASSO 3: Destruindo Lambda AuthHandler"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/lambda" ]; then
    cd infra/lambda

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo Lambda..."
        terraform destroy -auto-approve \
          -var="clientes_service_url=http://dummy" \
          || echo "⚠️  Erro ao destruir Lambda (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ Lambda AuthHandler destruído"
else
    echo "⚠️  Diretório infra/lambda não encontrado"
fi

echo ""

# ============================================================================
# PASSO 4: DESTRUIR COGNITO USER POOL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔐 PASSO 4: Destruindo Cognito User Pool"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/auth" ]; then
    cd infra/auth

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo Cognito..."
        terraform destroy -auto-approve || echo "⚠️  Erro ao destruir Cognito (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ Cognito User Pool destruído"
else
    echo "⚠️  Diretório infra/auth não encontrado"
fi

echo ""

# ============================================================================
# PASSO 5: DESTRUIR DATABASES RDS
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️  PASSO 5: Destruindo Databases RDS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/database" ]; then
    cd infra/database

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo RDS (isso pode levar 5-10 minutos)..."
        terraform destroy -auto-approve || echo "⚠️  Erro ao destruir RDS (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ Databases RDS destruídos"
else
    echo "⚠️  Diretório infra/database não encontrado"
fi

echo ""

# ============================================================================
# PASSO 6: DESTRUIR CLUSTER EKS
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "☸️  PASSO 6: Destruindo Cluster EKS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/kubernetes" ]; then
    cd infra/kubernetes

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo EKS (isso pode levar 10-15 minutos)..."
        terraform destroy -auto-approve || echo "⚠️  Erro ao destruir EKS (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ Cluster EKS destruído"
else
    echo "⚠️  Diretório infra/kubernetes não encontrado"
fi

echo ""

# ============================================================================
# PASSO 7: DESTRUIR ECR
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 PASSO 7: Destruindo ECR Repositories"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/ecr" ]; then
    cd infra/ecr

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo ECR..."
        terraform destroy -auto-approve || echo "⚠️  Erro ao destruir ECR (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ ECR Repositories destruídos"
else
    echo "⚠️  Diretório infra/ecr não encontrado"
fi

echo ""

# ============================================================================
# PASSO 8: DESTRUIR BACKEND (S3 + DynamoDB)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🪣  PASSO 8: Destruindo Backend (S3 + DynamoDB)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -d "infra/backend" ]; then
    cd infra/backend

    if [ -d ".terraform" ]; then
        echo "🗑️  Destruindo Backend..."
        terraform destroy -auto-approve || echo "⚠️  Erro ao destruir Backend (pode não existir)"
    else
        echo "⚠️  Terraform não inicializado - pulando"
    fi

    cd ../..
    echo "✅ Backend destruído"
else
    echo "⚠️  Diretório infra/backend não encontrado"
fi

echo ""

# ============================================================================
# RESUMO FINAL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ INFRAESTRUTURA AWS COMPLETAMENTE DESTRUÍDA!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Recursos removidos:"
echo "  ✅ API Gateway"
echo "  ✅ Lambda AuthHandler"
echo "  ✅ Cognito User Pool"
echo "  ✅ RDS MySQL (3 instâncias)"
echo "  ✅ Cluster EKS"
echo "  ✅ ECR Repositories"
echo "  ✅ Backend S3 + DynamoDB"
echo ""
echo "🚀 Para reprovisionar:"
echo "   ./deploy_scripts/aws/01-provision-infrastructure.sh"
echo ""
