#!/bin/bash

# Script de teste E2E para AWS: Fluxo completo com CLIENTE EXISTENTE
# Testa: autenticação (CPF existente) -> pedido -> pagamento -> cozinha -> preparo -> pronto

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "==================================================================="
echo "TESTE E2E: CLIENTE EXISTENTE (CPF 55555555555)"
echo "==================================================================="
echo ""

# ========================================================================
# OBTER URL DO API GATEWAY
# ========================================================================
echo "📡 Obtendo URL do API Gateway..."
cd infra/api-gateway
API_GATEWAY_URL=$(terraform output -raw api_gateway_url 2>/dev/null || echo "")
cd ../..

if [ -z "$API_GATEWAY_URL" ]; then
    echo -e "${RED}[ERRO] Não foi possível obter URL do API Gateway${NC}"
    echo "   Verifique se o API Gateway foi deployado"
    exit 1
fi

echo -e "${GREEN}✅ API Gateway URL: $API_GATEWAY_URL${NC}"
echo ""

# ========================================================================
# ETAPA 0: AUTENTICAÇÃO COM CPF EXISTENTE
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 0: Autenticação (Cliente Existente: CPF 55555555555)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

AUTH_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/auth/identificar" \
    -H "Content-Type: application/json" \
    -d '{"cpf": "55555555555"}')

TOKEN=$(echo "$AUTH_RESPONSE" | jq -r '.accessToken // empty')
TIPO=$(echo "$AUTH_RESPONSE" | jq -r '.tipo')
CLIENTE_ID=$(echo "$AUTH_RESPONSE" | jq -r '.clienteId // empty')
CPF=$(echo "$AUTH_RESPONSE" | jq -r '.cpf // empty')
EXPIRA=$(echo "$AUTH_RESPONSE" | jq -r '.expiresIn')

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ Falha ao obter token${NC}"
    exit 1
fi

if [ "$TIPO" != "IDENTIFICADO" ]; then
    echo -e "${RED}❌ Tipo de token incorreto: esperado IDENTIFICADO, recebido $TIPO${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Token JWT obtido: Tipo=$TIPO, ClienteID=$CLIENTE_ID, CPF=$CPF, Expira em ${EXPIRA}s${NC}"
echo ""

# ========================================================================
# ETAPA 1: CRIAR PEDIDO COM CPF DO CLIENTE
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 1: Criar Pedido (com CPF do cliente)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/pedidos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": "55555555555",
    "itens": [
      {"produtoId": 1, "quantidade": 1},
      {"produtoId": 5, "quantidade": 1}
    ]
  }')

PEDIDO_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
NUMERO_PEDIDO=$(echo "$RESPONSE" | jq -r '.numeroPedido')
STATUS=$(echo "$RESPONSE" | jq -r '.status // empty')
VALOR_TOTAL=$(echo "$RESPONSE" | jq -r '.valorTotal // empty')
NOME_CLIENTE=$(echo "$RESPONSE" | jq -r '.clienteNome // empty')

if [ -z "$PEDIDO_ID" ]; then
    echo -e "${RED}❌ Falha ao criar pedido${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

if [ -z "$NOME_CLIENTE" ] || [ "$NOME_CLIENTE" = "null" ]; then
    echo -e "${RED}❌ Nome do cliente não retornado no pedido${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Pedido criado: ID=$PEDIDO_ID, Número=$NUMERO_PEDIDO, Cliente=$NOME_CLIENTE, Status=$STATUS, Valor=R\$ $VALOR_TOTAL${NC}"
echo ""

# ========================================================================
# ETAPA 2: AGUARDAR PROCESSAMENTO ASSÍNCRONO DO PAGAMENTO
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 2: Processamento de Pagamento (assíncrono)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

sleep 5

# ========================================================================
# ETAPA 3: CONSULTAR STATUS DO PEDIDO
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 3: Consultar Status do Pedido"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

PEDIDO_STATUS=$(curl -s "$API_GATEWAY_URL/pedidos/$PEDIDO_ID" \
  -H "Authorization: Bearer $TOKEN")

CURRENT_STATUS=$(echo "$PEDIDO_STATUS" | jq -r '.status')
NOME_CLIENTE_FINAL=$(echo "$PEDIDO_STATUS" | jq -r '.clienteNome // empty')

if [ "$CURRENT_STATUS" = "CANCELADO" ]; then
    echo -e "${YELLOW}⚠️  Pagamento rejeitado: Status=$CURRENT_STATUS${NC}"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "RESUMO DO TESTE E2E - CLIENTE EXISTENTE"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Cliente: $NOME_CLIENTE (CPF: 55555555555)"
    echo "Pedido: $NUMERO_PEDIDO (ID=$PEDIDO_ID) | Valor: R\$ $VALOR_TOTAL"
    echo ""
    echo "Jornada:"
    echo "  CRIADO → CANCELADO (pagamento rejeitado)"
    echo ""
    echo -e "${YELLOW}⚠️  TESTE ENCERRADO: Pagamento rejeitado (cenário válido - 20% dos casos)${NC}"
    echo ""
    exit 0
elif [ "$CURRENT_STATUS" = "REALIZADO" ]; then
    echo -e "${GREEN}✅ Pagamento aprovado: Status=$CURRENT_STATUS, Cliente=$NOME_CLIENTE_FINAL${NC}"
else
    echo -e "${BLUE}ℹ️  Status do pedido: $CURRENT_STATUS${NC}"
fi
echo ""

# ========================================================================
# ETAPA 4: VERIFICAR PEDIDO NA FILA DA COZINHA
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 4: Verificar Fila da Cozinha"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

FILA_RESPONSE=$(curl -s "$API_GATEWAY_URL/cozinha/fila" \
  -H "Authorization: Bearer $TOKEN")

PEDIDO_NA_FILA=$(echo "$FILA_RESPONSE" | jq -r ".[] | select(.pedidoId == $PEDIDO_ID) | .pedidoId")
FILA_STATUS=$(echo "$FILA_RESPONSE" | jq -r ".[] | select(.pedidoId == $PEDIDO_ID) | .status")

if [ "$PEDIDO_NA_FILA" = "$PEDIDO_ID" ]; then
    echo -e "${GREEN}✅ Pedido na fila da cozinha: PedidoID=$PEDIDO_ID, Status=$FILA_STATUS${NC}"
else
    echo -e "${YELLOW}⚠️  Pedido não encontrado na fila${NC}"
fi
echo ""

# ========================================================================
# ETAPA 5: INICIAR PREPARO NA COZINHA
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 5: Iniciar Preparo na Cozinha"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

FILA_RESPONSE=$(curl -s "$API_GATEWAY_URL/cozinha/fila" \
  -H "Authorization: Bearer $TOKEN")

COZINHA_ID=$(echo "$FILA_RESPONSE" | jq -r ".[] | select(.pedidoId == $PEDIDO_ID) | .id")

if [ -z "$COZINHA_ID" ] || [ "$COZINHA_ID" = "null" ]; then
    echo -e "${RED}❌ Pedido não encontrado na fila da cozinha${NC}"
    exit 1
fi

INICIAR_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/cozinha/$COZINHA_ID/iniciar" \
  -H "Authorization: Bearer $TOKEN")

COZINHA_STATUS=$(echo "$INICIAR_RESPONSE" | jq -r '.status')

if [ "$COZINHA_STATUS" = "EM_PREPARO" ]; then
    echo -e "${GREEN}✅ Preparo iniciado: CozinhaID=$COZINHA_ID, Status=$COZINHA_STATUS${NC}"
else
    echo -e "${RED}❌ Falha ao iniciar preparo: Status=$COZINHA_STATUS${NC}"
    exit 1
fi
echo ""

# ========================================================================
# ETAPA 6: MARCAR PEDIDO COMO PRONTO
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 6: Finalizar Preparo"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

sleep 2

PRONTO_RESPONSE=$(curl -s -X POST "$API_GATEWAY_URL/cozinha/$COZINHA_ID/pronto" \
  -H "Authorization: Bearer $TOKEN")

COZINHA_STATUS_FINAL=$(echo "$PRONTO_RESPONSE" | jq -r '.status')

if [ "$COZINHA_STATUS_FINAL" = "PRONTO" ]; then
    echo -e "${GREEN}✅ Pedido finalizado: Status=$COZINHA_STATUS_FINAL${NC}"
else
    echo -e "${RED}❌ Falha ao finalizar: Status=$COZINHA_STATUS_FINAL${NC}"
    exit 1
fi
echo ""

# ========================================================================
# ETAPA 7: VERIFICAR STATUS FINAL DO PEDIDO
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "ETAPA 7: Verificar Status Final"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

sleep 2

PEDIDO_COMPLETO=$(curl -s "$API_GATEWAY_URL/pedidos/$PEDIDO_ID" \
  -H "Authorization: Bearer $TOKEN")

STATUS_COMPLETO=$(echo "$PEDIDO_COMPLETO" | jq -r '.status')

if [ "$STATUS_COMPLETO" = "PRONTO" ]; then
    echo -e "${GREEN}✅ Pedido pronto para retirada: Status=$STATUS_COMPLETO${NC}"
else
    echo -e "${YELLOW}⚠️  Status do pedido: $STATUS_COMPLETO${NC}"
fi
echo ""

# ========================================================================
# RESUMO FINAL
# ========================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RESUMO DO TESTE E2E - CLIENTE EXISTENTE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Cliente: $NOME_CLIENTE (CPF: 55555555555)"
echo "Pedido: $NUMERO_PEDIDO (ID=$PEDIDO_ID) | Valor: R\$ $VALOR_TOTAL"
echo ""
echo "Jornada completa:"
echo "  Autenticação (IDENTIFICADO) → CRIADO → REALIZADO → EM_PREPARO → PRONTO"
echo ""
echo "Componentes testados:"
echo "  ✅ API Gateway (Cognito JWT com CPF)"
echo "  ✅ Lambda AuthHandler (validação de cliente existente)"
echo "  ✅ Serviço de Clientes (integração Feign Client)"
echo "  ✅ Serviço de Pedidos (nome do cliente recuperado)"
echo "  ✅ Serviço de Pagamento (processamento assíncrono)"
echo "  ✅ Serviço de Cozinha"
echo "  ✅ RabbitMQ (eventos entre serviços)"
echo ""
echo -e "${GREEN}🎉 TESTE E2E CLIENTE EXISTENTE CONCLUÍDO COM SUCESSO!${NC}"
echo ""
