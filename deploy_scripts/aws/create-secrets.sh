#!/bin/bash

# Script para criar secrets do Kubernetes para deploy na AWS
# Extrai credenciais dos bancos RDS via Terraform outputs

set -e

echo "🔐 Criando Kubernetes Secrets para AWS..."
echo ""

# Navegar para o diretório do Terraform de database
TERRAFORM_DIR="$(cd "$(dirname "$0")/../../infra/database" && pwd)"

cd "$TERRAFORM_DIR"

# Verificar se o Terraform está inicializado
if [ ! -d ".terraform" ]; then
    echo "❌ Terraform não está inicializado em $TERRAFORM_DIR"
    echo "   Execute 'terraform init' primeiro"
    exit 1
fi

# Verificar se há state (pode estar no S3 backend)
# Tentamos obter um output para validar que o state existe e tem recursos
if ! terraform output mysql_clientes_endpoint &>/dev/null; then
    echo "❌ Não foi possível obter outputs do Terraform"
    echo "   Verifique se os bancos RDS foram provisionados com 'terraform apply'"
    exit 1
fi

echo "📊 Extraindo credenciais dos bancos RDS..."
echo ""

# Extrair outputs do Terraform
MYSQL_CLIENTES_ENDPOINT=$(terraform output -raw mysql_clientes_endpoint 2>/dev/null || echo "")
MYSQL_CLIENTES_ADDRESS=$(terraform output -raw mysql_clientes_address 2>/dev/null || echo "")
MYSQL_CLIENTES_PORT=$(terraform output -raw mysql_clientes_port 2>/dev/null || echo "")
MYSQL_CLIENTES_DATABASE=$(terraform output -raw mysql_clientes_database 2>/dev/null || echo "")
MYSQL_CLIENTES_USERNAME=$(terraform output -raw mysql_clientes_username 2>/dev/null || echo "")
MYSQL_CLIENTES_PASSWORD=$(terraform output -raw mysql_clientes_password 2>/dev/null || echo "")

MYSQL_PEDIDOS_ENDPOINT=$(terraform output -raw mysql_pedidos_endpoint 2>/dev/null || echo "")
MYSQL_PEDIDOS_ADDRESS=$(terraform output -raw mysql_pedidos_address 2>/dev/null || echo "")
MYSQL_PEDIDOS_PORT=$(terraform output -raw mysql_pedidos_port 2>/dev/null || echo "")
MYSQL_PEDIDOS_DATABASE=$(terraform output -raw mysql_pedidos_database 2>/dev/null || echo "")
MYSQL_PEDIDOS_USERNAME=$(terraform output -raw mysql_pedidos_username 2>/dev/null || echo "")
MYSQL_PEDIDOS_PASSWORD=$(terraform output -raw mysql_pedidos_password 2>/dev/null || echo "")

MYSQL_COZINHA_ENDPOINT=$(terraform output -raw mysql_cozinha_endpoint 2>/dev/null || echo "")
MYSQL_COZINHA_ADDRESS=$(terraform output -raw mysql_cozinha_address 2>/dev/null || echo "")
MYSQL_COZINHA_PORT=$(terraform output -raw mysql_cozinha_port 2>/dev/null || echo "")
MYSQL_COZINHA_DATABASE=$(terraform output -raw mysql_cozinha_database 2>/dev/null || echo "")
MYSQL_COZINHA_USERNAME=$(terraform output -raw mysql_cozinha_username 2>/dev/null || echo "")
MYSQL_COZINHA_PASSWORD=$(terraform output -raw mysql_cozinha_password 2>/dev/null || echo "")

# Validar que os outputs foram extraídos
if [ -z "$MYSQL_CLIENTES_ENDPOINT" ] || [ -z "$MYSQL_PEDIDOS_ENDPOINT" ] || [ -z "$MYSQL_COZINHA_ENDPOINT" ]; then
    echo "❌ Falha ao extrair outputs do Terraform"
    echo "   Verifique se os bancos RDS foram provisionados corretamente"
    exit 1
fi

echo "✅ Credenciais extraídas com sucesso!"
echo ""

# Criar secrets no Kubernetes
echo "🔧 Criando secrets no Kubernetes..."
echo ""

# MySQL Clientes Secret
echo "  → mysql-clientes-secret..."
kubectl create secret generic mysql-clientes-secret \
  --from-literal=MYSQL_HOST="$MYSQL_CLIENTES_ADDRESS" \
  --from-literal=MYSQL_PORT="$MYSQL_CLIENTES_PORT" \
  --from-literal=MYSQL_DATABASE="$MYSQL_CLIENTES_DATABASE" \
  --from-literal=MYSQL_USER="$MYSQL_CLIENTES_USERNAME" \
  --from-literal=MYSQL_PASSWORD="$MYSQL_CLIENTES_PASSWORD" \
  --from-literal=MYSQL_ROOT_PASSWORD="$MYSQL_CLIENTES_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

# MySQL Pedidos Secret
echo "  → mysql-pedidos-secret..."
kubectl create secret generic mysql-pedidos-secret \
  --from-literal=MYSQL_HOST="$MYSQL_PEDIDOS_ADDRESS" \
  --from-literal=MYSQL_PORT="$MYSQL_PEDIDOS_PORT" \
  --from-literal=MYSQL_DATABASE="$MYSQL_PEDIDOS_DATABASE" \
  --from-literal=MYSQL_USER="$MYSQL_PEDIDOS_USERNAME" \
  --from-literal=MYSQL_PASSWORD="$MYSQL_PEDIDOS_PASSWORD" \
  --from-literal=MYSQL_ROOT_PASSWORD="$MYSQL_PEDIDOS_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

# MySQL Cozinha Secret
echo "  → mysql-cozinha-secret..."
kubectl create secret generic mysql-cozinha-secret \
  --from-literal=MYSQL_HOST="$MYSQL_COZINHA_ADDRESS" \
  --from-literal=MYSQL_PORT="$MYSQL_COZINHA_PORT" \
  --from-literal=MYSQL_DATABASE="$MYSQL_COZINHA_DATABASE" \
  --from-literal=MYSQL_USER="$MYSQL_COZINHA_USERNAME" \
  --from-literal=MYSQL_PASSWORD="$MYSQL_COZINHA_PASSWORD" \
  --from-literal=MYSQL_ROOT_PASSWORD="$MYSQL_COZINHA_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

# MongoDB Secret (para pod MongoDB, valores padrão)
echo "  → mongodb-secret..."
kubectl create secret generic mongodb-secret \
  --from-literal=MONGO_INITDB_ROOT_USERNAME="admin" \
  --from-literal=MONGO_INITDB_ROOT_PASSWORD="admin123" \
  --from-literal=MONGO_USERNAME="pagamento_user" \
  --from-literal=MONGO_PASSWORD="pagamento123" \
  --dry-run=client -o yaml | kubectl apply -f -

# RabbitMQ Secret (para pod RabbitMQ, valores padrão)
echo "  → rabbitmq-secret..."
kubectl create secret generic rabbitmq-secret \
  --from-literal=RABBITMQ_DEFAULT_USER="admin" \
  --from-literal=RABBITMQ_DEFAULT_PASS="admin123" \
  --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "✅ Secrets criados com sucesso!"
echo ""

# Mostrar secrets criados
echo "📋 Secrets criados:"
kubectl get secrets | grep -E "(mysql|mongodb|rabbitmq)" || true
echo ""
