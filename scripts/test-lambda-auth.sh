#!/bin/bash

set -e

echo "🔑 TESTANDO LAMBDA DE AUTENTICAÇÃO"
echo "=================================="

# Verificar se jq está instalado
if ! command -v jq &> /dev/null; then
    echo "❌ jq não está instalado. Instale primeiro: sudo apt install jq"
    exit 1
fi

# Verificar se curl está instalado
if ! command -v curl &> /dev/null; then
    echo "❌ curl não está instalado. Instale primeiro: sudo apt install curl"
    exit 1
fi

# Obter URL da API Gateway (assumindo que já foi deployada)
if [[ -z "$API_GATEWAY_URL" ]]; then
    echo "⚠️  Variável API_GATEWAY_URL não definida. Tentando obter do Terraform..."
    
    if [[ -f "terraform/lambda/terraform.tfstate" ]]; then
        API_GATEWAY_URL=$(cd terraform/lambda && terraform output -raw api_gateway_url 2>/dev/null || echo "")
    fi
    
    if [[ -z "$API_GATEWAY_URL" ]]; then
        echo "❌ URL da API Gateway não encontrada. Configure API_GATEWAY_URL ou faça deploy primeiro."
        echo "💡 Exemplo: export API_GATEWAY_URL=https://abc123.execute-api.us-east-1.amazonaws.com/prod"
        exit 1
    fi
fi

echo "🌐 URL da API Gateway: $API_GATEWAY_URL"
echo ""

# Teste 1: Autenticação de cliente válido
echo "📋 TESTE 1: Autenticação de cliente válido"
echo "-------------------------------------------"

CLIENTE_REQUEST='{"cpf": "12345678901", "authType": "cliente"}'
CLIENTE_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/auth" \
    -H "Content-Type: application/json" \
    -d "$CLIENTE_REQUEST" || echo "ERRO_CONEXAO")

if [[ "$CLIENTE_RESPONSE" == "ERRO_CONEXAO" ]]; then
    echo "❌ Erro de conexão com API Gateway"
    exit 1
fi

echo "Request: $CLIENTE_REQUEST"
echo "Response: $CLIENTE_RESPONSE"

# Verificar se retornou token
CLIENTE_TOKEN=$(echo "$CLIENTE_RESPONSE" | jq -r '.token // empty' 2>/dev/null)
if [[ -z "$CLIENTE_TOKEN" ]]; then
    echo "❌ Token não retornado para cliente válido"
else
    echo "✅ Token recebido: ${CLIENTE_TOKEN:0:50}..."
fi

echo ""

# Teste 2: Autenticação anônima
echo "📋 TESTE 2: Autenticação anônima"
echo "--------------------------------"

ANONIMO_REQUEST='{"authType": "anonimo"}'
ANONIMO_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/auth" \
    -H "Content-Type: application/json" \
    -d "$ANONIMO_REQUEST")

echo "Request: $ANONIMO_REQUEST"
echo "Response: $ANONIMO_RESPONSE"

# Verificar se retornou token
ANONIMO_TOKEN=$(echo "$ANONIMO_RESPONSE" | jq -r '.token // empty' 2>/dev/null)
if [[ -z "$ANONIMO_TOKEN" ]]; then
    echo "❌ Token não retornado para usuário anônimo"
else
    echo "✅ Token recebido: ${ANONIMO_TOKEN:0:50}..."
fi

echo ""

# Teste 3: CPF inválido
echo "📋 TESTE 3: CPF inválido (deve retornar erro 400)"
echo "-------------------------------------------------"

CPF_INVALIDO_REQUEST='{"cpf": "123", "authType": "cliente"}'
CPF_INVALIDO_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$API_GATEWAY_URL/auth" \
    -H "Content-Type: application/json" \
    -d "$CPF_INVALIDO_REQUEST")

echo "Request: $CPF_INVALIDO_REQUEST"
echo "Response: $CPF_INVALIDO_RESPONSE"

HTTP_CODE=$(echo "$CPF_INVALIDO_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
if [[ "$HTTP_CODE" == "400" ]]; then
    echo "✅ Erro 400 retornado corretamente para CPF inválido"
else
    echo "❌ HTTP Code esperado: 400, recebido: $HTTP_CODE"
fi

echo ""

# Teste 4: Cliente inexistente
echo "📋 TESTE 4: Cliente inexistente (deve retornar erro 404)"
echo "-------------------------------------------------------"

CLIENTE_INEXISTENTE_REQUEST='{"cpf": "99999999999", "authType": "cliente"}'
CLIENTE_INEXISTENTE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$API_GATEWAY_URL/auth" \
    -H "Content-Type: application/json" \
    -d "$CLIENTE_INEXISTENTE_REQUEST")

echo "Request: $CLIENTE_INEXISTENTE_REQUEST"
echo "Response: $CLIENTE_INEXISTENTE_RESPONSE"

HTTP_CODE=$(echo "$CLIENTE_INEXISTENTE_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
if [[ "$HTTP_CODE" == "404" ]]; then
    echo "✅ Erro 404 retornado corretamente para cliente inexistente"
else
    echo "❌ HTTP Code esperado: 404, recebido: $HTTP_CODE"
fi

echo ""

# Teste 5: Payload inválido
echo "📋 TESTE 5: Payload inválido (deve retornar erro 500)"
echo "----------------------------------------------------"

PAYLOAD_INVALIDO='{json_malformado}'
PAYLOAD_INVALIDO_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$API_GATEWAY_URL/auth" \
    -H "Content-Type: application/json" \
    -d "$PAYLOAD_INVALIDO")

echo "Request: $PAYLOAD_INVALIDO"
echo "Response: $PAYLOAD_INVALIDO_RESPONSE"

HTTP_CODE=$(echo "$PAYLOAD_INVALIDO_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
if [[ "$HTTP_CODE" == "500" ]]; then
    echo "✅ Erro 500 retornado corretamente para payload inválido"
else
    echo "❌ HTTP Code esperado: 500, recebido: $HTTP_CODE"
fi

echo ""
echo "🏁 TESTES CONCLUÍDOS"
echo "===================="

# Resumo dos tokens obtidos
if [[ -n "$CLIENTE_TOKEN" && -n "$ANONIMO_TOKEN" ]]; then
    echo "✅ Ambos tokens obtidos com sucesso"
    echo "📋 Para usar nos testes da API:"
    echo "   export CLIENTE_TOKEN=\"$CLIENTE_TOKEN\""
    echo "   export ANONIMO_TOKEN=\"$ANONIMO_TOKEN\""
else
    echo "❌ Alguns tokens não foram obtidos. Verifique os erros acima."
fi