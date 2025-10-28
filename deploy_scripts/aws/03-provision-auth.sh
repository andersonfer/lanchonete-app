#!/bin/bash
set -e

echo "🔐 PROVISIONANDO INFRAESTRUTURA DE AUTENTICAÇÃO"
echo "================================================"
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

# Verifica se microserviços estão rodando
echo "📌 Verificando se microserviços estão deployados..."
if ! kubectl get svc clientes-service &>/dev/null; then
    echo "❌ Serviço Clientes não encontrado!"
    echo "   Execute primeiro: ./deploy_scripts/aws/02-deploy.sh"
    exit 1
fi

# Verifica se ALBs foram criados
echo "📌 Verificando ALBs criados..."
CLIENTES_ALB=$(aws elbv2 describe-load-balancers --names lanchonete-clientes-alb --query 'LoadBalancers[0].DNSName' --output text 2>/dev/null || echo "")
if [ -z "$CLIENTES_ALB" ] || [ "$CLIENTES_ALB" = "None" ]; then
    echo "❌ ALB do Clientes não encontrado!"
    echo "   O script 02-deploy.sh deve ter provisionado os ALBs"
    echo "   Aguarde alguns minutos para os ALBs serem criados"
    exit 1
fi

CLIENTES_URL="http://$CLIENTES_ALB"
echo "✅ URL do Clientes: $CLIENTES_URL"
echo ""

# ============================================================================
# PASSO 1: PROVISIONAR COGNITO USER POOL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔐 PASSO 1: Provisionando Cognito User Pool"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/auth

echo "🔨 Inicializando Terraform com backend S3..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve

echo ""
echo "✅ Cognito User Pool provisionado com sucesso!"
echo ""
echo "📋 Outputs:"
terraform output

cd ../..
echo ""

# ============================================================================
# PASSO 2: BUILD E DEPLOY LAMBDA AUTHHANDLER
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚡ PASSO 2: Build e Deploy Lambda AuthHandler"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/lambda

echo "🏗️  Buildando Lambda Java..."
./build.sh

echo ""
echo "🔨 Inicializando Terraform com backend S3..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve -var="clientes_service_url=$CLIENTES_URL"

echo ""
echo "✅ Lambda AuthHandler provisionada com sucesso!"
echo ""
echo "📋 Outputs:"
terraform output

cd ../..
echo ""

# ============================================================================
# PASSO 3: PROVISIONAR API GATEWAY
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 PASSO 3: Provisionando API Gateway"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📌 Obtendo URLs de todos os ALBs..."
PEDIDOS_ALB=$(aws elbv2 describe-load-balancers --names lanchonete-pedidos-alb --query 'LoadBalancers[0].DNSName' --output text 2>/dev/null || echo "")
COZINHA_ALB=$(aws elbv2 describe-load-balancers --names lanchonete-cozinha-alb --query 'LoadBalancers[0].DNSName' --output text 2>/dev/null || echo "")
PAGAMENTO_ALB=$(aws elbv2 describe-load-balancers --names lanchonete-pagamento-alb --query 'LoadBalancers[0].DNSName' --output text 2>/dev/null || echo "")

if [ -z "$PEDIDOS_ALB" ] || [ "$PEDIDOS_ALB" = "None" ] || \
   [ -z "$COZINHA_ALB" ] || [ "$COZINHA_ALB" = "None" ] || \
   [ -z "$PAGAMENTO_ALB" ] || [ "$PAGAMENTO_ALB" = "None" ]; then
    echo "❌ Algum ALB não foi encontrado"
    echo "   Clientes: $CLIENTES_ALB"
    echo "   Pedidos: $PEDIDOS_ALB"
    echo "   Cozinha: $COZINHA_ALB"
    echo "   Pagamento: $PAGAMENTO_ALB"
    echo ""
    echo "   Aguarde alguns minutos para os ALBs serem criados e tente novamente"
    exit 1
fi

PEDIDOS_URL="http://$PEDIDOS_ALB"
COZINHA_URL="http://$COZINHA_ALB"
PAGAMENTO_URL="http://$PAGAMENTO_ALB"

echo "✅ URLs dos ALBs obtidas:"
echo "   Clientes:  $CLIENTES_URL"
echo "   Pedidos:   $PEDIDOS_URL"
echo "   Cozinha:   $COZINHA_URL"
echo "   Pagamento: $PAGAMENTO_URL"
echo ""

cd infra/api-gateway

echo "🔨 Inicializando Terraform com backend S3..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve \
  -var="clientes_service_url=$CLIENTES_URL" \
  -var="pedidos_service_url=$PEDIDOS_URL" \
  -var="cozinha_service_url=$COZINHA_URL" \
  -var="pagamento_service_url=$PAGAMENTO_URL"

echo ""
echo "✅ API Gateway provisionado com sucesso!"
echo ""
echo "📋 Outputs:"
terraform output

API_URL=$(terraform output -raw api_gateway_url 2>/dev/null || echo "")
if [ -n "$API_URL" ]; then
    echo ""
    echo "🌐 URL da API: $API_URL"
fi

cd ../..
echo ""

# ============================================================================
# RESUMO FINAL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ INFRAESTRUTURA DE AUTENTICAÇÃO PROVISIONADA!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📦 Recursos criados:"
echo "  ✅ Cognito User Pool"
echo "  ✅ Lambda AuthHandler (URL: $CLIENTES_URL)"
echo "  ✅ API Gateway com Cognito Authorizer"
echo ""
if [ -n "$API_URL" ]; then
    echo "🌐 API Gateway URL: $API_URL"
    echo ""
fi
echo "🚀 Próximo passo:"
echo "   Testes de autenticação: ./deploy_scripts/aws/04-test-auth.sh"
echo ""
