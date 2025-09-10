#!/bin/bash

# Script para build e push das imagens Docker para ECR
# Uso: ./scripts/build-and-push.sh

set -e

echo "🔍 Coletando informações do ECR..."

# Obter registry ECR
ECR_REGISTRY=$(cd infra/ecr && terraform output -raw registry_url)
echo "  ECR Registry: $ECR_REGISTRY"

echo "🔐 Fazendo login no ECR..."
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $ECR_REGISTRY

echo "🏗️  Fazendo build das imagens..."

# Build autoatendimento
echo "  📦 Building autoatendimento..."
cd app/autoatendimento
docker build -t lanchonete-autoatendimento:latest .
docker tag lanchonete-autoatendimento:latest $ECR_REGISTRY/lanchonete-autoatendimento:latest
cd ../..

# Build pagamento
echo "  📦 Building pagamento..."
cd app/pagamento
docker build -t lanchonete-pagamento:latest .
docker tag lanchonete-pagamento:latest $ECR_REGISTRY/lanchonete-pagamento:latest
cd ../..

echo "🚀 Fazendo push das imagens para ECR..."

# Push autoatendimento
echo "  ⬆️  Pushing autoatendimento..."
docker push $ECR_REGISTRY/lanchonete-autoatendimento:latest

# Push pagamento
echo "  ⬆️  Pushing pagamento..."
docker push $ECR_REGISTRY/lanchonete-pagamento:latest

echo "✅ Build e push completos!"

echo "🔍 Verificando imagens no ECR..."
aws ecr describe-images --repository-name lanchonete-autoatendimento --query 'sort_by(imageDetails,& imagePushedAt)[-1].[imageTags[0],imagePushedAt]' --output table
aws ecr describe-images --repository-name lanchonete-pagamento --query 'sort_by(imageDetails,& imagePushedAt)[-1].[imageTags[0],imagePushedAt]' --output table

echo "📋 Imagens prontas para deploy no Kubernetes!"