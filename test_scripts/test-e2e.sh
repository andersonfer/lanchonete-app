#!/bin/bash

# Script de teste E2E: Fluxo completo do pedido
# Testa: pedido -> pagamento -> cozinha -> preparo -> pronto -> retirada

set -e

# URLs dos serviços
PEDIDOS_URL=$(minikube service pedidos-nodeport --url)
COZINHA_URL=$(minikube service cozinha-nodeport --url)

echo "==================================================================="
echo "TESTE E2E: FLUXO COMPLETO DO PEDIDO"
echo "==================================================================="
echo ""
echo "URLs dos serviços:"
echo "  Pedidos: $PEDIDOS_URL"
echo "  Cozinha: $COZINHA_URL"
echo ""

# ========================================================================
# ETAPA 1: CRIAR PEDIDO
# ========================================================================
echo "-------------------------------------------------------------------"
echo "ETAPA 1: Criar Pedido"
echo "-------------------------------------------------------------------"

echo ""
echo "Criando pedido anonimo..."
RESPONSE=$(curl -s -X POST ${PEDIDOS_URL}/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": null,
    "itens": [
      {"produtoId": 1, "quantidade": 1},
      {"produtoId": 5, "quantidade": 1}
    ]
  }')

echo "$RESPONSE" | jq .

PEDIDO_ID=$(echo "$RESPONSE" | jq -r '.id')
STATUS=$(echo "$RESPONSE" | jq -r '.status')
VALOR_TOTAL=$(echo "$RESPONSE" | jq -r '.valorTotal')

echo ""
echo "[OK] Pedido criado com sucesso!"
echo "  Pedido ID: $PEDIDO_ID"
echo "  Status: $STATUS"
echo "  Valor Total: R$ $VALOR_TOTAL"

# ========================================================================
# ETAPA 2: AGUARDAR PAGAMENTO E VERIFICAR FILA DA COZINHA
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "ETAPA 2: Processamento do Pagamento"
echo "-------------------------------------------------------------------"

echo ""
echo "Aguardando processamento do pagamento (5 segundos)..."
sleep 5

echo ""
echo "Verificando status do pedido apos pagamento..."
STATUS_RESPONSE=$(curl -s ${PEDIDOS_URL}/pedidos/${PEDIDO_ID})
STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')

echo "Status atual: $STATUS"

if [ "$STATUS" = "REALIZADO" ]; then
    echo "[OK] Pagamento aprovado!"
else
    echo "[ERRO] Status esperado: REALIZADO, obtido: $STATUS"
    exit 1
fi

echo ""
echo "Verificando fila da cozinha..."
FILA_RESPONSE=$(curl -s ${COZINHA_URL}/cozinha/fila)
PEDIDO_COZINHA=$(echo "$FILA_RESPONSE" | jq -r ".[] | select(.pedidoId == $PEDIDO_ID)")

if [ -n "$PEDIDO_COZINHA" ]; then
    COZINHA_ID=$(echo "$PEDIDO_COZINHA" | jq -r '.id')
    COZINHA_STATUS=$(echo "$PEDIDO_COZINHA" | jq -r '.status')
    echo "[OK] Pedido encontrado na fila da cozinha!"
    echo "  Cozinha ID: $COZINHA_ID"
    echo "  Status na cozinha: $COZINHA_STATUS"
else
    echo "[ERRO] Pedido nao encontrado na fila da cozinha"
    exit 1
fi

# ========================================================================
# ETAPA 3: INICIAR PREPARO
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "ETAPA 3: Iniciar Preparo"
echo "-------------------------------------------------------------------"

echo ""
echo "Iniciando preparo do pedido (Cozinha ID: $COZINHA_ID)..."
PREPARO_RESPONSE=$(curl -s -X POST ${COZINHA_URL}/cozinha/${COZINHA_ID}/iniciar)
echo "$PREPARO_RESPONSE" | jq .

COZINHA_STATUS=$(echo "$PREPARO_RESPONSE" | jq -r '.status')

if [ "$COZINHA_STATUS" = "EM_PREPARO" ]; then
    echo "[OK] Preparo iniciado!"
    echo "  Status na cozinha: $COZINHA_STATUS"
else
    echo "[ERRO] Status esperado: EM_PREPARO, obtido: $COZINHA_STATUS"
    exit 1
fi

# ========================================================================
# ETAPA 4: MARCAR COMO PRONTO
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "ETAPA 4: Marcar Pedido como Pronto"
echo "-------------------------------------------------------------------"

echo ""
echo "Aguardando 'preparo' do pedido (3 segundos)..."
sleep 3

echo ""
echo "Marcando pedido como pronto..."
PRONTO_RESPONSE=$(curl -s -X POST ${COZINHA_URL}/cozinha/${COZINHA_ID}/pronto)
echo "$PRONTO_RESPONSE" | jq .

COZINHA_STATUS=$(echo "$PRONTO_RESPONSE" | jq -r '.status')
DATA_FIM=$(echo "$PRONTO_RESPONSE" | jq -r '.dataFim')

if [ "$COZINHA_STATUS" = "PRONTO" ]; then
    echo "[OK] Pedido marcado como pronto!"
    echo "  Status na cozinha: $COZINHA_STATUS"
    echo "  Data de conclusao: $DATA_FIM"
else
    echo "[ERRO] Status esperado: PRONTO, obtido: $COZINHA_STATUS"
    exit 1
fi

# ========================================================================
# ETAPA 5: VERIFICAR STATUS NO SERVIÇO DE PEDIDOS
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "ETAPA 5: Verificar Atualizacao no Servico de Pedidos"
echo "-------------------------------------------------------------------"

echo ""
echo "Aguardando propagacao do evento (3 segundos)..."
sleep 3

echo ""
echo "Verificando status do pedido no servico de pedidos..."
STATUS_RESPONSE=$(curl -s ${PEDIDOS_URL}/pedidos/${PEDIDO_ID})
STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')

echo "Status atual: $STATUS"

if [ "$STATUS" = "PRONTO" ]; then
    echo "[OK] Evento processado! Status atualizado para PRONTO"
else
    echo "[ERRO] Status esperado: PRONTO, obtido: $STATUS"
    exit 1
fi

# ========================================================================
# ETAPA 6: RETIRAR PEDIDO
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "ETAPA 6: Retirar Pedido"
echo "-------------------------------------------------------------------"

echo ""
echo "Marcando pedido como retirado..."
RETIRAR_RESPONSE=$(curl -s -X PATCH ${PEDIDOS_URL}/pedidos/${PEDIDO_ID}/retirar)
echo "$RETIRAR_RESPONSE" | jq .

STATUS=$(echo "$RETIRAR_RESPONSE" | jq -r '.status')

if [ "$STATUS" = "FINALIZADO" ]; then
    echo "[OK] Pedido retirado com sucesso!"
    echo "  Status final: $STATUS"
else
    echo "[ERRO] Status esperado: FINALIZADO, obtido: $STATUS"
    exit 1
fi

# ========================================================================
# RESUMO FINAL
# ========================================================================
echo ""
echo "==================================================================="
echo "RESUMO DO TESTE"
echo "==================================================================="
echo ""
echo "[OK] Todas as etapas foram concluidas com sucesso!"
echo ""
echo "Fluxo testado:"
echo "  1. [OK] Pedido criado (ID: $PEDIDO_ID) - Status: CRIADO"
echo "  2. [OK] Pagamento aprovado - Status: REALIZADO"
echo "  3. [OK] Pedido adicionado a fila da cozinha - Status: AGUARDANDO"
echo "  4. [OK] Preparo iniciado - Status: EM_PREPARO"
echo "  5. [OK] Pedido marcado como pronto - Status: PRONTO"
echo "  6. [OK] Evento propagado para servico de pedidos - Status: PRONTO"
echo "  7. [OK] Pedido retirado - Status: FINALIZADO"
echo ""
echo "==================================================================="
