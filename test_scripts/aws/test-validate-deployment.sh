#!/bin/bash
set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 VALIDAÇÃO END-TO-END DO DEPLOYMENT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

ERRORS=0

# ==============================================================================
# TESTE 1: Verificar Recursos Terraform
# ==============================================================================
echo "1️⃣  Verificando recursos Terraform..."
echo ""

MODULES=("backend" "ecr" "kubernetes" "database" "ingress" "auth" "lambda" "api-gateway")
for module in "${MODULES[@]}"; do
    if [ -d "infra/$module" ]; then
        cd "infra/$module"
        STATE_EXISTS=$(terraform show 2>&1 | grep -c "No state" || true)
        if [ "$STATE_EXISTS" -eq 0 ]; then
            echo "  ✅ $module: provisionado"
        else
            echo "  ❌ $module: não provisionado"
            ((ERRORS++))
        fi
        cd ../..
    fi
done
echo ""

# ==============================================================================
# TESTE 2: Verificar ALBs
# ==============================================================================
echo "2️⃣  Verificando ALBs..."
echo ""

ALB_NAMES=("lanchonete-clientes-alb" "lanchonete-pedidos-alb" "lanchonete-cozinha-alb" "lanchonete-pagamento-alb")
for alb in "${ALB_NAMES[@]}"; do
    STATUS=$(aws elbv2 describe-load-balancers --names "$alb" --query 'LoadBalancers[0].State.Code' --output text 2>/dev/null || echo "notfound")
    if [ "$STATUS" = "active" ]; then
        echo "  ✅ $alb: active"
    else
        echo "  ❌ $alb: $STATUS"
        ((ERRORS++))
    fi
done
echo ""

# ==============================================================================
# TESTE 3: Verificar API Gateway
# ==============================================================================
echo "3️⃣  Verificando API Gateway..."
echo ""

cd infra/api-gateway
API_URL=$(terraform output -raw api_gateway_url 2>/dev/null || echo "")
cd ../..

if [ -z "$API_URL" ]; then
    echo "  ❌ API Gateway URL não encontrada"
    ((ERRORS++))
else
    echo "  ✅ API Gateway URL: $API_URL"

    # Testar endpoint de autenticação (deve estar acessível)
    AUTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/auth/identificar" \
        -X POST \
        -H "Content-Type: application/json" \
        -d '{"cpf": null}')

    if [ "$AUTH_STATUS" = "200" ]; then
        echo "  ✅ Endpoint de autenticação: acessível"
    else
        echo "  ❌ Endpoint de autenticação: HTTP $AUTH_STATUS"
        ((ERRORS++))
    fi
fi
echo ""

# ==============================================================================
# TESTE 4: Autenticação e JWT
# ==============================================================================
echo "4️⃣  Testando autenticação..."
echo ""

if [ -n "$API_URL" ]; then
    AUTH_RESPONSE=$(curl -s -X POST "$API_URL/auth/identificar" \
        -H "Content-Type: application/json" \
        -d '{"cpf": null}')

    TOKEN=$(echo "$AUTH_RESPONSE" | jq -r '.accessToken // empty')

    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        echo "  ✅ Token JWT obtido: ${TOKEN:0:30}..."
    else
        echo "  ❌ Falha ao obter token JWT"
        echo "     Response: $AUTH_RESPONSE"
        ((ERRORS++))
        TOKEN=""
    fi
fi
echo ""

# ==============================================================================
# TESTE 5: Endpoints de Negócio
# ==============================================================================
echo "5️⃣  Testando endpoints de negócio..."
echo ""

if [ -n "$TOKEN" ] && [ -n "$API_URL" ]; then
    # Teste POST /clientes (gerar CPF de 11 dígitos)
    RANDOM_CPF="$(date +%s)$(shuf -i 0-9 -n 1)"
    RANDOM_CPF="${RANDOM_CPF:0:11}"

    CREATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$API_URL/clientes" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"nome\": \"Teste Validação\", \"cpf\": \"$RANDOM_CPF\", \"email\": \"test@validation.com\"}")

    CREATE_STATUS=$(echo "$CREATE_RESPONSE" | grep HTTP_STATUS | cut -d: -f2)

    if [ "$CREATE_STATUS" = "201" ]; then
        echo "  ✅ POST /clientes: HTTP 201"

        # Extrair CPF do cliente criado
        CLIENT_CPF=$(echo "$CREATE_RESPONSE" | grep -v HTTP_STATUS | jq -r '.cpf')

        # Teste GET /clientes/cpf/{cpf}
        GET_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/clientes/cpf/$CLIENT_CPF" \
            -H "Authorization: Bearer $TOKEN")

        if [ "$GET_STATUS" = "200" ]; then
            echo "  ✅ GET /clientes/cpf/{cpf}: HTTP 200"
        else
            echo "  ❌ GET /clientes/cpf/{cpf}: HTTP $GET_STATUS"
            ((ERRORS++))
        fi

        # Teste POST /clientes/identificar
        IDENT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_URL/clientes/identificar" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "{\"cpf\": \"$CLIENT_CPF\"}")

        if [ "$IDENT_STATUS" = "200" ]; then
            echo "  ✅ POST /clientes/identificar: HTTP 200"
        else
            echo "  ❌ POST /clientes/identificar: HTTP $IDENT_STATUS"
            ((ERRORS++))
        fi
    else
        echo "  ❌ POST /clientes: HTTP $CREATE_STATUS"
        ((ERRORS++))
    fi
fi
echo ""

# ==============================================================================
# TESTE 6: Segurança (Acesso sem Token)
# ==============================================================================
echo "6️⃣  Testando segurança (acesso sem token)..."
echo ""

if [ -n "$API_URL" ]; then
    UNAUTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/clientes")

    if [ "$UNAUTH_STATUS" = "401" ] || [ "$UNAUTH_STATUS" = "403" ]; then
        echo "  ✅ Acesso sem token bloqueado: HTTP $UNAUTH_STATUS"
    else
        echo "  ❌ Acesso sem token NÃO bloqueado: HTTP $UNAUTH_STATUS"
        ((ERRORS++))
    fi
fi
echo ""

# ==============================================================================
# TESTE 7: Conectividade dos Serviços
# ==============================================================================
echo "7️⃣  Testando conectividade dos serviços..."
echo ""

if [ -n "$TOKEN" ] && [ -n "$API_URL" ]; then
    # Teste Clientes - tentar buscar CPF inexistente (404 é válido)
    CLIENTES_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/clientes/cpf/99999999999" \
        -H "Authorization: Bearer $TOKEN")

    if [ "$CLIENTES_STATUS" = "200" ] || [ "$CLIENTES_STATUS" = "404" ]; then
        echo "  ✅ clientes: acessível (HTTP $CLIENTES_STATUS)"
    else
        echo "  ⚠️  clientes: HTTP $CLIENTES_STATUS"
    fi

    # Teste Pedidos - tentar buscar pedido inexistente (404 é válido)
    PEDIDOS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/pedidos/99999" \
        -H "Authorization: Bearer $TOKEN")

    if [ "$PEDIDOS_STATUS" = "200" ] || [ "$PEDIDOS_STATUS" = "404" ]; then
        echo "  ✅ pedidos: acessível (HTTP $PEDIDOS_STATUS)"
    else
        echo "  ⚠️  pedidos: HTTP $PEDIDOS_STATUS"
    fi
fi
echo ""

# ==============================================================================
# RESULTADO FINAL
# ==============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $ERRORS -eq 0 ]; then
    echo "✅ VALIDAÇÃO CONCLUÍDA COM SUCESSO!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "🎉 Todos os testes passaram!"
    echo ""
    echo "📊 Resumo:"
    echo "  ✅ Infraestrutura Terraform: OK"
    echo "  ✅ ALBs: ativos"
    echo "  ✅ API Gateway: acessível"
    echo "  ✅ Autenticação JWT: funcionando"
    echo "  ✅ Endpoints de negócio: funcionando"
    echo "  ✅ Segurança: configurada corretamente"
    echo ""
    exit 0
else
    echo "❌ VALIDAÇÃO FALHOU"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "⚠️  $ERRORS erro(s) encontrado(s)"
    echo ""
    echo "Por favor, revise os logs acima e corrija os problemas."
    echo ""
    exit 1
fi
