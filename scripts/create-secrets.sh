#!/bin/bash

# Script para criar Secrets do Kubernetes automaticamente
# Uso: ./scripts/create-secrets.sh

set -e

echo "🔍 Coletando credenciais do RDS..."

# Obter dados do RDS do Terraform
cd infra/database
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
DATABASE_NAME=$(terraform output -raw database_name)
DATABASE_USERNAME=$(terraform output -raw database_username)
DATABASE_PASSWORD=$(terraform output -raw database_password)
cd ../..

# Construir JDBC URL
JDBC_URL="jdbc:mysql://${RDS_ENDPOINT}/${DATABASE_NAME}"

echo "📝 Dados coletados:"
echo "  RDS Endpoint: $RDS_ENDPOINT"
echo "  Database: $DATABASE_NAME"
echo "  Username: $DATABASE_USERNAME"
echo "  Password: [HIDDEN]"
echo "  JDBC URL: $JDBC_URL"

echo "🔐 Criando Secret 'rds-secret'..."

# Deletar secret se já existir (para recriar)
kubectl delete secret rds-secret --ignore-not-found=true

# Criar o secret
kubectl create secret generic rds-secret \
  --from-literal=SPRING_DATASOURCE_URL="$JDBC_URL" \
  --from-literal=SPRING_DATASOURCE_USERNAME="$DATABASE_USERNAME" \
  --from-literal=SPRING_DATASOURCE_PASSWORD="$DATABASE_PASSWORD"

echo "✅ Secret 'rds-secret' criado com sucesso!"

echo "🔍 Verificando Secret criado..."
kubectl describe secret rds-secret

echo "📋 Secret pronto para uso nos deployments!"