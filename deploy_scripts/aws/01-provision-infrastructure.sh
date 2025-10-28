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
# PASSO 4: PROVISIONAR BANCOS DE DADOS RDS
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️  PASSO 4: Provisionando Bancos de Dados RDS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/database

echo "🔨 Inicializando Terraform com backend S3..."
terraform init

echo ""
echo "🚀 Aplicando configuração (isso pode levar 5-10 minutos)..."
terraform apply -auto-approve

echo ""
echo "✅ Bancos RDS provisionados com sucesso!"
echo ""
echo "📋 Endpoints criados:"
terraform output -json all_endpoints | jq -r 'to_entries[] | "  ✅ \(.key): \(.value)"'

cd ../..
echo ""

# ============================================================================
# PASSO 5: CONFIGURAR KUBECTL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚙️  PASSO 5: Configurando kubectl"
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
# PASSO 6: PROVISIONAR AWS LOAD BALANCER CONTROLLER
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔀 PASSO 6: Provisionando AWS Load Balancer Controller"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/ingress

echo "🔨 Inicializando Terraform com backend S3..."
terraform init

echo ""
echo "🚀 Aplicando configuração (instala AWS Load Balancer Controller via Helm)..."
terraform apply -auto-approve -var="cluster_name=$CLUSTER_NAME"

echo ""
echo "✅ AWS Load Balancer Controller provisionado com sucesso!"
echo ""
echo "📋 Verificando deployment do controller:"
kubectl get deployment -n kube-system aws-load-balancer-controller || echo "⚠️  Controller ainda iniciando..."

cd ../..
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
echo "  ✅ RDS MySQL: 3 instâncias (clientes, pedidos, cozinha)"
echo "  ✅ kubectl configurado"
echo "  ✅ AWS Load Balancer Controller instalado"
echo ""
echo "🚀 Próximos passos:"
echo "   1. Deploy dos microserviços: ./deploy_scripts/aws/02-deploy.sh"
echo "   2. Provisionar autenticação: ./deploy_scripts/aws/03-provision-auth.sh"
echo "   3. Testes de autenticação: ./deploy_scripts/aws/04-test-auth.sh"
echo ""
