#!/bin/bash
set -e

echo "🧪 TESTING AUTHENTICATION SCENARIOS"
echo "===================================="
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para testar endpoint
test_endpoint() {
    local description=$1
    local method=$2
    local url=$3
    local data=$4
    local auth_header=$5
    local expected_status=$6

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "${BLUE}TEST: $description${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Request: $method $url"

    if [ -n "$data" ]; then
        echo "Body: $data"
    fi

    if [ -n "$auth_header" ]; then
        echo "Authorization: Bearer <token>"
    fi

    echo ""

    # Executar request
    local response
    local http_code

    if [ -n "$auth_header" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X $method "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $auth_header" \
                -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$url" \
                -H "Authorization: Bearer $auth_header")
        fi
    else
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X $method "$url" \
                -H "Content-Type: application/json" \
                -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$url")
        fi
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    echo "Response Status: $http_code"
    echo "Response Body:"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
    echo ""

    # Verificar resultado
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✅ PASS - Status code correto ($http_code)${NC}"
        return 0
    else
        echo -e "${RED}❌ FAIL - Esperado: $expected_status, Recebido: $http_code${NC}"
        return 1
    fi
}

# ============================================================================
# OBTER API GATEWAY URL
# ============================================================================
echo "📡 Obtendo URL do API Gateway..."
cd infra/api-gateway
API_GATEWAY_URL=$(terraform output -raw api_gateway_url 2>/dev/null || echo "")
cd ../..

if [ -z "$API_GATEWAY_URL" ]; then
    echo "❌ Erro: Não foi possível obter URL do API Gateway"
    echo "   Verifique se o API Gateway foi deployado"
    exit 1
fi

echo "✅ API Gateway URL: $API_GATEWAY_URL"
echo ""

# Variáveis de controle
PASSED=0
FAILED=0

# ============================================================================
# CENÁRIO 1: ACESSO SEM AUTENTICAÇÃO (deve falhar)
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  CENÁRIO 1: Acesso sem autenticação (deve retornar 401)  ║"
echo "╚═══════════════════════════════════════════════════════════╝"

if test_endpoint \
    "Acessar /clientes sem token" \
    "GET" \
    "$API_GATEWAY_URL/clientes/actuator/health" \
    "" \
    "" \
    "401"; then
    PASSED=$((PASSED + 1))
else
    FAILED=$((FAILED + 1))
fi

# ============================================================================
# CENÁRIO 2: AUTENTICAÇÃO ANÔNIMA
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  CENÁRIO 2: Autenticação Anônima (sem CPF)               ║"
echo "╚═══════════════════════════════════════════════════════════╝"

# Obter token anônimo
echo "🔑 Obtendo token anônimo..."
ANON_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/auth/identificar" \
    -H "Content-Type: application/json" \
    -d '{"cpf": ""}')

ANON_TOKEN=$(echo "$ANON_RESPONSE" | jq -r '.accessToken // empty')

if [ -z "$ANON_TOKEN" ]; then
    echo -e "${RED}❌ FAIL - Não foi possível obter token anônimo${NC}"
    echo "Response: $ANON_RESPONSE"
    FAILED=$((FAILED + 1))
else
    echo "✅ Token anônimo obtido"
    echo "   Tipo: $(echo "$ANON_RESPONSE" | jq -r '.tipo')"
    echo "   Expira em: $(echo "$ANON_RESPONSE" | jq -r '.expiresIn')s"
    PASSED=$((PASSED + 1))

    # Testar acesso com token anônimo
    if test_endpoint \
        "Acessar /clientes com token anônimo" \
        "GET" \
        "$API_GATEWAY_URL/clientes/actuator/health" \
        "" \
        "$ANON_TOKEN" \
        "200"; then
        PASSED=$((PASSED + 1))
    else
        FAILED=$((FAILED + 1))
    fi
fi

# ============================================================================
# CENÁRIO 3: AUTENTICAÇÃO COM CPF
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  CENÁRIO 3: Autenticação com CPF                         ║"
echo "╚═══════════════════════════════════════════════════════════╝"

# CPF de teste
TEST_CPF="12345678900"

echo "🔑 Obtendo token para CPF: $TEST_CPF..."
CPF_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/auth/identificar" \
    -H "Content-Type: application/json" \
    -d "{\"cpf\": \"$TEST_CPF\"}")

CPF_TOKEN=$(echo "$CPF_RESPONSE" | jq -r '.accessToken // empty')

if [ -z "$CPF_TOKEN" ]; then
    echo -e "${RED}❌ FAIL - Não foi possível obter token para CPF${NC}"
    echo "Response: $CPF_RESPONSE"
    FAILED=$((FAILED + 1))
else
    echo "✅ Token CPF obtido"
    echo "   Cliente ID: $(echo "$CPF_RESPONSE" | jq -r '.clienteId')"
    echo "   Tipo: $(echo "$CPF_RESPONSE" | jq -r '.tipo')"
    echo "   Expira em: $(echo "$CPF_RESPONSE" | jq -r '.expiresIn')s"
    PASSED=$((PASSED + 1))

    # Testar acesso aos 4 microserviços
    for service in clientes pedidos cozinha pagamento; do
        if test_endpoint \
            "Acessar /$service com token CPF" \
            "GET" \
            "$API_GATEWAY_URL/$service/actuator/health" \
            "" \
            "$CPF_TOKEN" \
            "200"; then
            PASSED=$((PASSED + 1))
        else
            FAILED=$((FAILED + 1))
        fi
    done
fi

# ============================================================================
# CENÁRIO 4: CRIAÇÃO DE CLIENTE VIA API
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  CENÁRIO 4: Criar cliente via API Gateway                ║"
echo "╚═══════════════════════════════════════════════════════════╝"

if [ -n "$CPF_TOKEN" ]; then
    RANDOM_CPF="$(date +%s)$(shuf -i 100-999 -n 1)"
    RANDOM_CPF="${RANDOM_CPF:0:11}"

    if test_endpoint \
        "Criar novo cliente" \
        "POST" \
        "$API_GATEWAY_URL/clientes/clientes" \
        "{\"cpf\": \"$RANDOM_CPF\", \"nome\": \"Cliente Teste\", \"email\": \"teste@lanchonete.com\"}" \
        "$CPF_TOKEN" \
        "201"; then
        PASSED=$((PASSED + 1))

        # Buscar cliente criado
        if test_endpoint \
            "Buscar cliente criado" \
            "GET" \
            "$API_GATEWAY_URL/clientes/clientes/cpf/$RANDOM_CPF" \
            "" \
            "$CPF_TOKEN" \
            "200"; then
            PASSED=$((PASSED + 1))
        else
            FAILED=$((FAILED + 1))
        fi
    else
        FAILED=$((FAILED + 1))
    fi
else
    echo -e "${YELLOW}⏭️  SKIP - Token CPF não disponível${NC}"
fi

# ============================================================================
# CENÁRIO 5: TOKEN INVÁLIDO
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  CENÁRIO 5: Token Inválido (deve retornar 401/403)      ║"
echo "╚═══════════════════════════════════════════════════════════╝"

INVALID_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

response=$(curl -s -w "\n%{http_code}" -X GET "$API_GATEWAY_URL/clientes/actuator/health" \
    -H "Authorization: Bearer $INVALID_TOKEN")

http_code=$(echo "$response" | tail -n1)

echo "Request: GET $API_GATEWAY_URL/clientes/actuator/health"
echo "Authorization: Bearer <invalid_token>"
echo ""
echo "Response Status: $http_code"
echo ""

if [ "$http_code" -eq "401" ] || [ "$http_code" -eq "403" ]; then
    echo -e "${GREEN}✅ PASS - Token inválido rejeitado corretamente${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "${RED}❌ FAIL - Esperado: 401 ou 403, Recebido: $http_code${NC}"
    FAILED=$((FAILED + 1))
fi

# ============================================================================
# RESUMO DOS TESTES
# ============================================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 RESUMO DOS TESTES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

TOTAL=$((PASSED + FAILED))
PASS_RATE=$(awk "BEGIN {printf \"%.1f\", ($PASSED / $TOTAL) * 100}")

echo -e "Total de testes: $TOTAL"
echo -e "${GREEN}✅ Passed: $PASSED${NC}"
echo -e "${RED}❌ Failed: $FAILED${NC}"
echo -e "Taxa de sucesso: $PASS_RATE%"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 Todos os testes passaram!${NC}"
    echo ""
    exit 0
else
    echo -e "${RED}⚠️  Alguns testes falharam. Verifique os logs acima.${NC}"
    echo ""
    echo "💡 Dicas de troubleshooting:"
    echo "   1. Verifique logs do Lambda:"
    echo "      aws logs tail /aws/lambda/lanchonete-auth-lambda --follow"
    echo ""
    echo "   2. Verifique se os LoadBalancers estão acessíveis:"
    echo "      kubectl get svc"
    echo ""
    echo "   3. Verifique logs dos microserviços:"
    echo "      kubectl logs -l app=clientes"
    echo ""
    exit 1
fi
