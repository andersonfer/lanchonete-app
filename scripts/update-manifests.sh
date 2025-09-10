#!/bin/bash

# Script para atualizar manifests Kubernetes com valores dinâmicos do Terraform
# Uso: ./scripts/update-manifests.sh

set -e

echo "🔍 Coletando informações da infraestrutura..."

# Obter registry ECR
ECR_REGISTRY=$(cd infra/ecr && terraform output -raw registry_url)

# Obter endpoint RDS  
RDS_ENDPOINT=$(cd infra/database && terraform output -raw rds_endpoint)

echo "📝 Valores coletados:"
echo "  ECR Registry: $ECR_REGISTRY"
echo "  RDS Endpoint: $RDS_ENDPOINT"

echo "🔄 Atualizando manifests..."

# Substituir ECR_REGISTRY nos manifests
find k8s_manifests -name "*.yaml" -o -name "*.yml" | while read file; do
    if grep -q "ECR_REGISTRY" "$file"; then
        echo "  Atualizando $file"
        sed -i "s|ECR_REGISTRY|${ECR_REGISTRY}|g" "$file"
    fi
done

# Substituir RDS_ENDPOINT nos manifests (se houver)
find k8s_manifests -name "*.yaml" -o -name "*.yml" | while read file; do
    if grep -q "RDS_ENDPOINT" "$file"; then
        echo "  Atualizando $file"
        sed -i "s|RDS_ENDPOINT|${RDS_ENDPOINT}|g" "$file"
    fi
done

echo "✅ Manifests atualizados com sucesso!"
echo ""
echo "📋 Próximos passos:"
echo "  1. Fazer build e push das imagens Docker"
echo "  2. Criar Secrets do RDS"
echo "  3. Aplicar manifests no cluster"