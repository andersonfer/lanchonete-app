#!/bin/bash

set -e

echo "📤 PUSHING IMAGES TO ECR"
echo "========================"

# Verificar variáveis de ambiente
if [ -z "$AWS_ACCOUNT_ID" ]; then
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    echo "📋 AWS Account ID detectado: $AWS_ACCOUNT_ID"
fi

if [ -z "$AWS_REGION" ]; then
    AWS_REGION="us-east-1"
    echo "🌍 Usando região padrão: $AWS_REGION"
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Login no ECR
echo "🔐 Fazendo login no ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

# Criar repositórios ECR se não existirem
echo "📦 Criando repositórios ECR..."
aws ecr describe-repositories --repository-names lanchonete/autoatendimento --region $AWS_REGION 2>/dev/null || \
    aws ecr create-repository --repository-name lanchonete/autoatendimento --region $AWS_REGION

aws ecr describe-repositories --repository-names lanchonete/pagamento --region $AWS_REGION 2>/dev/null || \
    aws ecr create-repository --repository-name lanchonete/pagamento --region $AWS_REGION

# Tag e push autoatendimento
echo "🍔 1/2 - Pushing autoatendimento..."
docker tag lanchonete/autoatendimento:latest ${ECR_REGISTRY}/lanchonete/autoatendimento:latest
docker push ${ECR_REGISTRY}/lanchonete/autoatendimento:latest

# Tag e push pagamento
echo "💳 2/2 - Pushing pagamento..."
docker tag lanchonete/pagamento:latest ${ECR_REGISTRY}/lanchonete/pagamento:latest
docker push ${ECR_REGISTRY}/lanchonete/pagamento:latest

echo ""
echo "✅ TODAS AS IMAGENS ENVIADAS PARA O ECR!"
echo "========================================"
echo "📍 Registry: $ECR_REGISTRY"
echo "🍔 autoatendimento: ${ECR_REGISTRY}/lanchonete/autoatendimento:latest"
echo "💳 pagamento: ${ECR_REGISTRY}/lanchonete/pagamento:latest"