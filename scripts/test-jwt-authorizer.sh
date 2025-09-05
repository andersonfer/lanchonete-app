#!/bin/bash

# Script de teste para JWT Authorizer - ETAPA 3
# Tests all authorization scenarios

set -e

echo "🧪 TESTANDO JWT AUTHORIZER - ETAPA 3"
echo "===================================="

# Configurações
API_BASE_URL="https://xxzm6g5y17.execute-api.us-east-1.amazonaws.com/v1"

echo ""
echo "🔑 1. OBTENDO TOKENS DE TESTE..."

# Obter token cliente
echo "   Obtendo token cliente..."
CUSTOMER_RESPONSE=$(curl -s -X POST "$API_BASE_URL/auth" \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901", "authType": "cliente"}')

CUSTOMER_TOKEN=$(echo "$CUSTOMER_RESPONSE" | jq -r '.token')

if [[ $CUSTOMER_TOKEN == "null" ]]; then
    echo "❌ Erro ao obter token cliente: $CUSTOMER_RESPONSE"
    exit 1
fi

echo "   ✅ Token cliente obtido: ${CUSTOMER_TOKEN:0:50}..."

# Obter token anônimo
echo "   Obtendo token anônimo..."
ANON_RESPONSE=$(curl -s -X POST "$API_BASE_URL/auth" \
  -H "Content-Type: application/json" \
  -d '{"authType": "anonimo"}')

ANON_TOKEN=$(echo "$ANON_RESPONSE" | jq -r '.token')

if [[ $ANON_TOKEN == "null" ]]; then
    echo "❌ Erro ao obter token anônimo: $ANON_RESPONSE"
    exit 1
fi

echo "   ✅ Token anônimo obtido: ${ANON_TOKEN:0:50}..."

echo ""
echo "🛡️ 2. TESTANDO AUTORIZAÇÃO..."

# Teste 1: Token cliente válido
echo ""
echo "   📋 Teste 1: Token cliente válido → GET /produtos/categoria/LANCHE"
RESPONSE1=$(curl -s -w "\n%{http_code}" -X GET "$API_BASE_URL/produtos/categoria/LANCHE" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")

HTTP_CODE1=$(echo "$RESPONSE1" | tail -n1)
BODY1=$(echo "$RESPONSE1" | head -n -1)

echo "   Status: $HTTP_CODE1"
echo "   Response: $BODY1" | jq . 2>/dev/null || echo "   Response: $BODY1"

if [[ $HTTP_CODE1 == "200" ]]; then
    echo "   ✅ SUCESSO: Autorização cliente funcionando"
else
    echo "   ❌ ERRO: Esperado 200, recebido $HTTP_CODE1"
fi

# Teste 2: Token anônimo válido
echo ""
echo "   📋 Teste 2: Token anônimo válido → GET /clientes"
RESPONSE2=$(curl -s -w "\n%{http_code}" -X GET "$API_BASE_URL/clientes" \
  -H "Authorization: Bearer $ANON_TOKEN")

HTTP_CODE2=$(echo "$RESPONSE2" | tail -n1)
BODY2=$(echo "$RESPONSE2" | head -n -1)

echo "   Status: $HTTP_CODE2"
echo "   Response: $BODY2" | jq . 2>/dev/null || echo "   Response: $BODY2"

if [[ $HTTP_CODE2 == "200" ]]; then
    echo "   ✅ SUCESSO: Autorização anônima funcionando"
else
    echo "   ❌ ERRO: Esperado 200, recebido $HTTP_CODE2"
fi

# Teste 3: Token inválido
echo ""
echo "   📋 Teste 3: Token inválido → GET /pedidos"
RESPONSE3=$(curl -s -w "\n%{http_code}" -X GET "$API_BASE_URL/pedidos" \
  -H "Authorization: Bearer token-invalido")

HTTP_CODE3=$(echo "$RESPONSE3" | tail -n1)
BODY3=$(echo "$RESPONSE3" | head -n -1)

echo "   Status: $HTTP_CODE3"
echo "   Response: $BODY3"

if [[ $HTTP_CODE3 == "401" ]]; then
    echo "   ✅ SUCESSO: Token inválido rejeitado corretamente"
else
    echo "   ❌ ERRO: Esperado 401, recebido $HTTP_CODE3"
fi

# Teste 4: Sem token
echo ""
echo "   📋 Teste 4: Sem token → GET /pedidos"
RESPONSE4=$(curl -s -w "\n%{http_code}" -X GET "$API_BASE_URL/pedidos")

HTTP_CODE4=$(echo "$RESPONSE4" | tail -n1)
BODY4=$(echo "$RESPONSE4" | head -n -1)

echo "   Status: $HTTP_CODE4"
echo "   Response: $BODY4"

if [[ $HTTP_CODE4 == "401" ]]; then
    echo "   ✅ SUCESSO: Requisição sem token rejeitada corretamente"
else
    echo "   ❌ ERRO: Esperado 401, recebido $HTTP_CODE4"
fi

# Teste 5: Rota pública (auth) ainda acessível
echo ""
echo "   📋 Teste 5: Rota pública /auth ainda acessível"
RESPONSE5=$(curl -s -w "\n%{http_code}" -X POST "$API_BASE_URL/auth" \
  -H "Content-Type: application/json" \
  -d '{"tipoAuth": "anonimo"}')

HTTP_CODE5=$(echo "$RESPONSE5" | tail -n1)
BODY5=$(echo "$RESPONSE5" | head -n -1)

echo "   Status: $HTTP_CODE5"
echo "   Response: $BODY5" | jq -r '.authType' 2>/dev/null || echo "   Response: $BODY5"

if [[ $HTTP_CODE5 == "200" ]]; then
    echo "   ✅ SUCESSO: Rota /auth pública funcionando"
else
    echo "   ❌ ERRO: Esperado 200, recebido $HTTP_CODE5"
fi

echo ""
echo "🎯 3. VALIDANDO CONTEXT INJECTION..."

# Verificar se os dados do context estão sendo retornados no mock
echo "   Verificando dados do cliente no context..."
CLIENT_DATA=$(echo "$BODY1" | jq -r '.headers.clienteId' 2>/dev/null)
if [[ $CLIENT_DATA == "1" ]]; then
    echo "   ✅ Context injection cliente funcionando (clienteId: $CLIENT_DATA)"
else
    echo "   ❌ Context injection cliente não funcionando"
fi

echo "   Verificando dados anônimos no context..."  
ANON_DATA=$(echo "$BODY2" | jq -r '.headers.authType' 2>/dev/null)
if [[ $ANON_DATA == "anonimo" ]]; then
    echo "   ✅ Context injection anônimo funcionando (authType: $ANON_DATA)"
else
    echo "   ❌ Context injection anônimo não funcionando"
fi

echo ""
echo "📊 RESUMO DOS TESTES:"
echo "==================="

TOTAL_TESTS=5
PASSED_TESTS=0

[[ $HTTP_CODE1 == "200" ]] && ((PASSED_TESTS++))
[[ $HTTP_CODE2 == "200" ]] && ((PASSED_TESTS++))
[[ $HTTP_CODE3 == "401" ]] && ((PASSED_TESTS++))
[[ $HTTP_CODE4 == "401" ]] && ((PASSED_TESTS++))
[[ $HTTP_CODE5 == "200" ]] && ((PASSED_TESTS++))

echo "✅ Testes passaram: $PASSED_TESTS/$TOTAL_TESTS"

if [[ $PASSED_TESTS == $TOTAL_TESTS ]]; then
    echo "🎉 TODOS OS TESTES PASSARAM! JWT Authorizer funcionando perfeitamente."
    exit 0
else
    echo "❌ Alguns testes falharam. Verifique a configuração."
    exit 1
fi