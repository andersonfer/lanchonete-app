#!/bin/bash
set -e

echo "🔐 DEPLOYING AUTHENTICATION STACK"
echo "=================================="
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

# Verifica contexto kubectl
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
echo "📌 Current kubectl context: $CURRENT_CONTEXT"
if [[ ! "$CURRENT_CONTEXT" =~ "eks" ]] && [[ ! "$CURRENT_CONTEXT" =~ "lanchonete" ]]; then
    echo "⚠️  Warning: Context doesn't look like EKS"
    echo "   Run: aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster"
    read -p "   Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
echo ""

# ============================================================================
# PASSO 1: OBTER URLs DOS LOADBALANCERS
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 PASSO 1: Obtendo URLs dos LoadBalancers"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "⏳ Aguardando LoadBalancers ficarem prontos..."
echo "   (pode levar alguns minutos se recém-criados)"
echo ""

# Função para obter URL do LoadBalancer
get_lb_url() {
    local service_name=$1
    local max_attempts=30
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        local hostname=$(kubectl get svc ${service_name}-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")

        if [ -n "$hostname" ]; then
            echo "http://${hostname}:8080"
            return 0
        fi

        attempt=$((attempt + 1))
        if [ $attempt -lt $max_attempts ]; then
            sleep 10
        fi
    done

    echo "ERROR"
    return 1
}

# Obter URLs
echo "📡 Obtendo URL do serviço Clientes..."
CLIENTES_URL=$(get_lb_url "clientes")
if [ "$CLIENTES_URL" == "ERROR" ]; then
    echo "❌ Erro: Não foi possível obter URL do LoadBalancer do serviço Clientes"
    echo "   Verifique se o serviço foi deployado: kubectl get svc clientes-service"
    exit 1
fi
echo "   ✅ Clientes: $CLIENTES_URL"

echo "📡 Obtendo URL do serviço Pedidos..."
PEDIDOS_URL=$(get_lb_url "pedidos")
if [ "$PEDIDOS_URL" == "ERROR" ]; then
    echo "❌ Erro: Não foi possível obter URL do LoadBalancer do serviço Pedidos"
    exit 1
fi
echo "   ✅ Pedidos: $PEDIDOS_URL"

echo "📡 Obtendo URL do serviço Cozinha..."
COZINHA_URL=$(get_lb_url "cozinha")
if [ "$COZINHA_URL" == "ERROR" ]; then
    echo "❌ Erro: Não foi possível obter URL do LoadBalancer do serviço Cozinha"
    exit 1
fi
echo "   ✅ Cozinha: $COZINHA_URL"

echo "📡 Obtendo URL do serviço Pagamento..."
PAGAMENTO_URL=$(get_lb_url "pagamento")
if [ "$PAGAMENTO_URL" == "ERROR" ]; then
    echo "❌ Erro: Não foi possível obter URL do LoadBalancer do serviço Pagamento"
    exit 1
fi
echo "   ✅ Pagamento: $PAGAMENTO_URL"

echo ""
echo "✅ Todas as URLs dos LoadBalancers obtidas com sucesso!"
echo ""

# ============================================================================
# PASSO 2: BUILD DO LAMBDA JAR
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 PASSO 2: Compilando Lambda (Java)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/lambda
echo "🔨 Executando build.sh..."
./build.sh

echo ""
echo "✅ Lambda JAR compilado e empacotado!"
cd ../..
echo ""

# ============================================================================
# PASSO 3: DEPLOY DO COGNITO
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔑 PASSO 3: Provisionando Cognito User Pool"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/auth

echo "🔨 Inicializando Terraform..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve

echo ""
echo "✅ Cognito User Pool provisionado!"

# Obter outputs do Cognito
USER_POOL_ID=$(terraform output -raw user_pool_id 2>/dev/null || echo "")
USER_POOL_CLIENT_ID=$(terraform output -raw user_pool_client_id 2>/dev/null || echo "")

if [ -n "$USER_POOL_ID" ]; then
    echo "   User Pool ID: $USER_POOL_ID"
    echo "   Client ID: $USER_POOL_CLIENT_ID"
fi

cd ../..
echo ""

# ============================================================================
# PASSO 4: DEPLOY DO LAMBDA
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚡ PASSO 4: Provisionando Lambda Function"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/lambda

echo "🔨 Inicializando Terraform..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve \
    -var="clientes_service_url=${CLIENTES_URL}"

echo ""
echo "✅ Lambda Function provisionada!"

# Obter Lambda ARN
LAMBDA_ARN=$(terraform output -raw lambda_function_arn 2>/dev/null || echo "")
LAMBDA_INVOKE_ARN=$(terraform output -raw lambda_invoke_arn 2>/dev/null || echo "")

if [ -n "$LAMBDA_ARN" ]; then
    echo "   Lambda ARN: $LAMBDA_ARN"
fi

cd ../..
echo ""

# ============================================================================
# PASSO 5: DEPLOY DO API GATEWAY
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚪 PASSO 5: Provisionando API Gateway"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

cd infra/api-gateway

echo "🔨 Inicializando Terraform..."
terraform init

echo ""
echo "🚀 Aplicando configuração..."
terraform apply -auto-approve \
    -var="clientes_service_url=${CLIENTES_URL}" \
    -var="pedidos_service_url=${PEDIDOS_URL}" \
    -var="cozinha_service_url=${COZINHA_URL}" \
    -var="pagamento_service_url=${PAGAMENTO_URL}"

echo ""
echo "✅ API Gateway provisionado!"

# Obter API Gateway URL
API_GATEWAY_URL=$(terraform output -raw api_gateway_url 2>/dev/null || echo "")

cd ../..
echo ""

# ============================================================================
# RESUMO FINAL
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ AUTHENTICATION STACK DEPLOYED!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📦 Recursos criados:"
echo "  ✅ Cognito User Pool: $USER_POOL_ID"
echo "  ✅ Lambda Function: lanchonete-auth-lambda"
echo "  ✅ API Gateway: lanchonete-api"
echo ""

if [ -n "$API_GATEWAY_URL" ]; then
    echo "🌐 API Gateway URL:"
    echo "   $API_GATEWAY_URL"
    echo ""
    echo "📋 Endpoints disponíveis:"
    echo "   POST $API_GATEWAY_URL/auth/identificar"
    echo "        (público - para obter token)"
    echo ""
    echo "   ANY  $API_GATEWAY_URL/clientes/*"
    echo "   ANY  $API_GATEWAY_URL/pedidos/*"
    echo "   ANY  $API_GATEWAY_URL/cozinha/*"
    echo "   ANY  $API_GATEWAY_URL/pagamento/*"
    echo "        (protegido - requer token no header Authorization)"
    echo ""
else
    echo "⚠️  Não foi possível obter URL do API Gateway"
    echo "   Execute: cd infra/api-gateway && terraform output api_gateway_url"
    echo ""
fi

echo "🔍 LoadBalancers dos microserviços:"
echo "   Clientes:  $CLIENTES_URL"
echo "   Pedidos:   $PEDIDOS_URL"
echo "   Cozinha:   $COZINHA_URL"
echo "   Pagamento: $PAGAMENTO_URL"
echo ""

echo "🧪 Próximo passo - Testar autenticação:"
echo "   ./deploy_scripts/aws/test-auth-scenarios.sh"
echo ""

echo "💡 Comandos úteis:"
echo "   # Ver logs do Lambda"
echo "   aws logs tail /aws/lambda/lanchonete-auth-lambda --follow"
echo ""
echo "   # Ver detalhes do User Pool"
echo "   aws cognito-idp describe-user-pool --user-pool-id $USER_POOL_ID"
echo ""
echo "   # Ver API Gateway"
echo "   aws apigateway get-rest-apis"
echo ""
