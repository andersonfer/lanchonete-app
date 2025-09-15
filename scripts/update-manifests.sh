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

# Substituir qualquer registry ECR existente pelo registry correto
# Padrão: [ACCOUNT_ID].dkr.ecr.us-east-1.amazonaws.com
find k8s_manifests -name "*.yaml" -o -name "*.yml" | while read file; do
    # Verifica se o arquivo contém algum registry ECR
    if grep -q "[0-9]\{12\}\.dkr\.ecr\.us-east-1\.amazonaws\.com" "$file"; then
        echo "  Atualizando registry ECR em $file"
        # Substitui qualquer ID de conta AWS (12 dígitos) pelo registry correto
        sed -i "s|[0-9]\{12\}\.dkr\.ecr\.us-east-1\.amazonaws\.com|${ECR_REGISTRY}|g" "$file"
    fi
done

# Substituir RDS_ENDPOINT nos manifests (se houver placeholder)
find k8s_manifests -name "*.yaml" -o -name "*.yml" | while read file; do
    if grep -q "RDS_ENDPOINT" "$file"; then
        echo "  Atualizando RDS endpoint em $file"
        sed -i "s|RDS_ENDPOINT|${RDS_ENDPOINT}|g" "$file"
    fi
done

echo "✅ Manifests atualizados com sucesso!"
echo ""
echo "📋 Próximos passos:"
echo "  1. Fazer build e push das imagens Docker"
echo "  2. Criar Secrets do RDS"
echo "  3. Aplicar manifests no cluster"