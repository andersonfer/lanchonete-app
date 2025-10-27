#!/bin/bash
set -e

echo "🏗️  PROVISIONANDO INFRAESTRUTURA AWS"
echo "===================================="
echo ""

# Verifica AWS credentials
echo "📌 Verificando credenciais AWS..."
if ! aws sts get-caller-identity &>/dev/null; then
    echo "❌ Credenciais AWS inválidas ou expiradas"
    echo "   Configure as credenciais e tente novamente"
    exit 1
fi

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "✅ AWS Account ID: $ACCOUNT_ID"
echo ""

# ============================================================================
# PASSO 1: PROVISIONAR BACKEND (S3 + DynamoDB)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 PASSO 1: Provisionando Backend (S3 + DynamoDB)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/backend

# Verifica se já existe
BUCKET_EXISTS=$(aws s3 ls s3://lanchonete-terraform-state-poc 2>&1 | grep -c "lanchonete-terraform-state-poc" || true)

if [ "$BUCKET_EXISTS" -gt 0 ]; then
    echo "✅ Backend já provisionado"
else
    echo "🔨 Inicializando Terraform..."
    terraform init

    echo ""
    echo "🚀 Aplicando configuração..."
    terraform apply -auto-approve

    echo ""
    echo "✅ Backend provisionado com sucesso!"
fi

cd ../..
echo ""

# ============================================================================
# PASSO 2: PROVISIONAR ECR (Repositórios de Imagens)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 PASSO 2: Provisionando ECR (Repositórios)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/ecr

echo "🔨 Inicializando Terraform..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve

echo ""
echo "✅ ECR provisionado com sucesso!"

cd ../..
echo ""

# ============================================================================
# PASSO 3: PROVISIONAR CLUSTER EKS
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "☸️  PASSO 3: Provisionando Cluster EKS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/kubernetes

echo "🔨 Inicializando Terraform com backend S3..."
terraform init

echo ""
echo "🚀 Aplicando configuração (isso pode levar 10-15 minutos)..."
terraform apply -auto-approve

CLUSTER_NAME=$(terraform output -raw cluster_name 2>/dev/null || echo "lanchonete-cluster")
echo ""
echo "✅ Cluster EKS provisionado: $CLUSTER_NAME"

cd ../..
echo ""

# ============================================================================
# PASSO 4: CONFIGURAR KUBECTL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚙️  PASSO 4: Configurando kubectl"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📌 Atualizando kubeconfig..."
aws eks update-kubeconfig --region us-east-1 --name $CLUSTER_NAME

echo ""
echo "🔍 Verificando conectividade..."
kubectl cluster-info

echo ""
echo "📋 Nodes do cluster:"
kubectl get nodes

echo ""

# ============================================================================
# RESUMO FINAL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ INFRAESTRUTURA AWS PROVISIONADA COM SUCESSO!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📦 Recursos criados:"
echo "  ✅ Backend S3: lanchonete-terraform-state-poc"
echo "  ✅ DynamoDB: lanchonete-terraform-locks"
echo "  ✅ ECR: 4 repositórios (clientes, pedidos, cozinha, pagamento)"
echo "  ✅ Cluster EKS: $CLUSTER_NAME"
echo "  ✅ kubectl configurado"
echo ""
echo "🚀 Próximo passo:"
echo "   ./deploy_scripts/aws/deploy.sh"
echo ""
