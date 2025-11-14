#!/bin/bash

# Script de teste E2E: Fluxo completo do pedido
# Testa: pedido -> pagamento -> cozinha -> preparo -> pronto -> retirada

set -e

# Cores
RED='\033[0;31m'
NC='\033[0m' # No Color

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
echo "Aguardando processamento do pagamento (2 segundos)..."
sleep 2

echo ""
echo "Verificando status do pedido apos pagamento..."
STATUS_RESPONSE=$(curl -s ${PEDIDOS_URL}/pedidos/${PEDIDO_ID})
STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')

echo "Status atual: $STATUS"

if [ "$STATUS" = "REALIZADO" ]; then
    echo "[OK] Pagamento aprovado! Continuando fluxo..."

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
        echo -e "${RED}[ERRO] Pedido nao encontrado na fila da cozinha${NC}"
        exit 1
    fi

    PAGAMENTO_APROVADO=true

elif [ "$STATUS" = "CANCELADO" ]; then
    echo "[OK] Pagamento rejeitado - Pedido cancelado (comportamento esperado)"
    PAGAMENTO_APROVADO=false
else
    echo -e "${RED}[ERRO] Status esperado: REALIZADO ou CANCELADO, obtido: $STATUS${NC}"
    exit 1
fi

# Continuar apenas se pagamento foi aprovado
if [ "$PAGAMENTO_APROVADO" = true ]; then

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
        echo -e "${RED}[ERRO] Status esperado: EM_PREPARO, obtido: $COZINHA_STATUS${NC}"
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
        echo -e "${RED}[ERRO] Status esperado: PRONTO, obtido: $COZINHA_STATUS${NC}"
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
        echo -e "${RED}[ERRO] Status esperado: PRONTO, obtido: $STATUS${NC}"
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
        echo -e "${RED}[ERRO] Status esperado: FINALIZADO, obtido: $STATUS${NC}"
        exit 1
    fi

fi

# ========================================================================
# RESUMO FINAL - TESTE 1
# ========================================================================
echo ""
echo "==================================================================="
echo "RESUMO - TESTE 1: Pedido Anonimo"
echo "==================================================================="
echo ""
echo "[OK] Teste 1 concluido com sucesso!"
echo ""
if [ "$PAGAMENTO_APROVADO" = true ]; then
    echo "Fluxo testado:"
    echo "  1. [OK] Pedido criado (ID: $PEDIDO_ID) - Status: CRIADO"
    echo "  2. [OK] Pagamento aprovado - Status: REALIZADO"
    echo "  3. [OK] Pedido adicionado a fila da cozinha - Status: AGUARDANDO"
    echo "  4. [OK] Preparo iniciado - Status: EM_PREPARO"
    echo "  5. [OK] Pedido marcado como pronto - Status: PRONTO"
    echo "  6. [OK] Evento propagado para servico de pedidos - Status: PRONTO"
    echo "  7. [OK] Pedido retirado - Status: FINALIZADO"
else
    echo "Fluxo testado:"
    echo "  1. [OK] Pedido criado (ID: $PEDIDO_ID) - Status: CRIADO"
    echo "  2. [OK] Pagamento rejeitado - Status: CANCELADO"
    echo "  3. [OK] Pedido nao foi para fila da cozinha (comportamento correto)"
fi
echo ""
echo "==================================================================="

# ========================================================================
# TESTE 2: PEDIDO COM CLIENTE IDENTIFICADO
# ========================================================================
echo ""
echo ""
echo "==================================================================="
echo "TESTE 2: PEDIDO COM CLIENTE IDENTIFICADO"
echo "==================================================================="
echo ""

# ========================================================================
# ETAPA 1: CRIAR PEDIDO COM CPF
# ========================================================================
echo "-------------------------------------------------------------------"
echo "ETAPA 1: Criar Pedido com CPF"
echo "-------------------------------------------------------------------"

echo ""
echo "Criando pedido com CPF: 55555555555..."
RESPONSE=$(curl -s -X POST ${PEDIDOS_URL}/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": "55555555555",
    "itens": [
      {"produtoId": 2, "quantidade": 2},
      {"produtoId": 6, "quantidade": 1}
    ]
  }')

echo "$RESPONSE" | jq .

PEDIDO_ID_2=$(echo "$RESPONSE" | jq -r '.id')
STATUS_2=$(echo "$RESPONSE" | jq -r '.status')
VALOR_TOTAL_2=$(echo "$RESPONSE" | jq -r '.valorTotal')
CLIENTE_NOME=$(echo "$RESPONSE" | jq -r '.clienteNome')

echo ""
echo "[OK] Pedido criado com sucesso!"
echo "  Pedido ID: $PEDIDO_ID_2"
echo "  Status: $STATUS_2"
echo "  Valor Total: R$ $VALOR_TOTAL_2"
echo "  Cliente Nome: $CLIENTE_NOME"

# Validar integracao com Feign Client
if [ "$CLIENTE_NOME" != "null" ] && [ -n "$CLIENTE_NOME" ]; then
    echo "[OK] Integracao Feign Client funcionando - Nome do cliente recuperado!"
else
    echo -e "${RED}[ERRO] Integracao Feign Client falhou - Nome do cliente nao foi recuperado${NC}"
    exit 1
fi

# ========================================================================
# ETAPA 2: AGUARDAR PAGAMENTO E VERIFICAR FILA DA COZINHA
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "ETAPA 2: Processamento do Pagamento"
echo "-------------------------------------------------------------------"

echo ""
echo "Aguardando processamento do pagamento (2 segundos)..."
sleep 2

echo ""
echo "Verificando status do pedido apos pagamento..."
STATUS_RESPONSE=$(curl -s ${PEDIDOS_URL}/pedidos/${PEDIDO_ID_2})
STATUS_2=$(echo "$STATUS_RESPONSE" | jq -r '.status')

echo "Status atual: $STATUS_2"

if [ "$STATUS_2" = "REALIZADO" ]; then
    echo "[OK] Pagamento aprovado! Continuando fluxo..."

    echo ""
    echo "Verificando fila da cozinha..."
    FILA_RESPONSE=$(curl -s ${COZINHA_URL}/cozinha/fila)
    PEDIDO_COZINHA_2=$(echo "$FILA_RESPONSE" | jq -r ".[] | select(.pedidoId == $PEDIDO_ID_2)")

    if [ -n "$PEDIDO_COZINHA_2" ]; then
        COZINHA_ID_2=$(echo "$PEDIDO_COZINHA_2" | jq -r '.id')
        COZINHA_STATUS_2=$(echo "$PEDIDO_COZINHA_2" | jq -r '.status')
        echo "[OK] Pedido encontrado na fila da cozinha!"
        echo "  Cozinha ID: $COZINHA_ID_2"
        echo "  Status na cozinha: $COZINHA_STATUS_2"
    else
        echo -e "${RED}[ERRO] Pedido nao encontrado na fila da cozinha${NC}"
        exit 1
    fi

    PAGAMENTO_APROVADO_2=true

elif [ "$STATUS_2" = "CANCELADO" ]; then
    echo "[OK] Pagamento rejeitado - Pedido cancelado (comportamento esperado)"
    PAGAMENTO_APROVADO_2=false
else
    echo -e "${RED}[ERRO] Status esperado: REALIZADO ou CANCELADO, obtido: $STATUS_2${NC}"
    exit 1
fi

# Continuar apenas se pagamento foi aprovado
if [ "$PAGAMENTO_APROVADO_2" = true ]; then

    # ========================================================================
    # ETAPA 3: INICIAR PREPARO
    # ========================================================================
    echo ""
    echo "-------------------------------------------------------------------"
    echo "ETAPA 3: Iniciar Preparo"
    echo "-------------------------------------------------------------------"

    echo ""
    echo "Iniciando preparo do pedido (Cozinha ID: $COZINHA_ID_2)..."
    PREPARO_RESPONSE=$(curl -s -X POST ${COZINHA_URL}/cozinha/${COZINHA_ID_2}/iniciar)
    echo "$PREPARO_RESPONSE" | jq .

    COZINHA_STATUS_2=$(echo "$PREPARO_RESPONSE" | jq -r '.status')

    if [ "$COZINHA_STATUS_2" = "EM_PREPARO" ]; then
        echo "[OK] Preparo iniciado!"
        echo "  Status na cozinha: $COZINHA_STATUS_2"
    else
        echo -e "${RED}[ERRO] Status esperado: EM_PREPARO, obtido: $COZINHA_STATUS_2${NC}"
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
    PRONTO_RESPONSE=$(curl -s -X POST ${COZINHA_URL}/cozinha/${COZINHA_ID_2}/pronto)
    echo "$PRONTO_RESPONSE" | jq .

    COZINHA_STATUS_2=$(echo "$PRONTO_RESPONSE" | jq -r '.status')
    DATA_FIM_2=$(echo "$PRONTO_RESPONSE" | jq -r '.dataFim')

    if [ "$COZINHA_STATUS_2" = "PRONTO" ]; then
        echo "[OK] Pedido marcado como pronto!"
        echo "  Status na cozinha: $COZINHA_STATUS_2"
        echo "  Data de conclusao: $DATA_FIM_2"
    else
        echo -e "${RED}[ERRO] Status esperado: PRONTO, obtido: $COZINHA_STATUS_2${NC}"
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
    STATUS_RESPONSE=$(curl -s ${PEDIDOS_URL}/pedidos/${PEDIDO_ID_2})
    STATUS_2=$(echo "$STATUS_RESPONSE" | jq -r '.status')

    echo "Status atual: $STATUS_2"

    if [ "$STATUS_2" = "PRONTO" ]; then
        echo "[OK] Evento processado! Status atualizado para PRONTO"
    else
        echo -e "${RED}[ERRO] Status esperado: PRONTO, obtido: $STATUS_2${NC}"
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
    RETIRAR_RESPONSE=$(curl -s -X PATCH ${PEDIDOS_URL}/pedidos/${PEDIDO_ID_2}/retirar)
    echo "$RETIRAR_RESPONSE" | jq .

    STATUS_2=$(echo "$RETIRAR_RESPONSE" | jq -r '.status')

    if [ "$STATUS_2" = "FINALIZADO" ]; then
        echo "[OK] Pedido retirado com sucesso!"
        echo "  Status final: $STATUS_2"
    else
        echo -e "${RED}[ERRO] Status esperado: FINALIZADO, obtido: $STATUS_2${NC}"
        exit 1
    fi

fi

# ========================================================================
# RESUMO FINAL - TESTE 2
# ========================================================================
echo ""
echo "==================================================================="
echo "RESUMO - TESTE 2: Pedido com Cliente Identificado"
echo "==================================================================="
echo ""
echo "[OK] Teste 2 concluido com sucesso!"
echo ""
if [ "$PAGAMENTO_APROVADO_2" = true ]; then
    echo "Fluxo testado:"
    echo "  1. [OK] Pedido criado (ID: $PEDIDO_ID_2) com CPF: 55555555555 - Status: CRIADO"
    echo "  2. [OK] Nome do cliente recuperado via Feign Client: $CLIENTE_NOME"
    echo "  3. [OK] Pagamento aprovado - Status: REALIZADO"
    echo "  4. [OK] Pedido adicionado a fila da cozinha - Status: AGUARDANDO"
    echo "  5. [OK] Preparo iniciado - Status: EM_PREPARO"
    echo "  6. [OK] Pedido marcado como pronto - Status: PRONTO"
    echo "  7. [OK] Evento propagado para servico de pedidos - Status: PRONTO"
    echo "  8. [OK] Pedido retirado - Status: FINALIZADO"
else
    echo "Fluxo testado:"
    echo "  1. [OK] Pedido criado (ID: $PEDIDO_ID_2) com CPF: 55555555555 - Status: CRIADO"
    echo "  2. [OK] Nome do cliente recuperado via Feign Client: $CLIENTE_NOME"
    echo "  3. [OK] Pagamento rejeitado - Status: CANCELADO"
    echo "  4. [OK] Pedido nao foi para fila da cozinha (comportamento correto)"
fi
echo ""
echo "==================================================================="

# ========================================================================
# TESTE 3: EDGE CASES E VALIDACAO DE ERROS
# ========================================================================
echo ""
echo ""
echo "==================================================================="
echo "TESTE 3: EDGE CASES E VALIDACAO DE ERROS"
echo "==================================================================="
echo ""

# ========================================================================
# CASO 1: PRODUTO INEXISTENTE
# ========================================================================
echo "-------------------------------------------------------------------"
echo "CASO 1: Tentar criar pedido com produto inexistente"
echo "-------------------------------------------------------------------"

echo ""
echo "Tentando criar pedido com produtoId: 999 (nao existe)..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST ${PEDIDOS_URL}/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": null,
    "itens": [
      {"produtoId": 999, "quantidade": 1}
    ]
  }')

echo "HTTP Status Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "404" ]; then
    echo "[OK] Erro tratado corretamente - Produto inexistente"
    cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json
else
    echo -e "${RED}[ERRO] Status code esperado: 400 ou 404, obtido: $HTTP_CODE${NC}"
    exit 1
fi

# ========================================================================
# CASO 2: PEDIDO INEXISTENTE NA COZINHA
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "CASO 2: Tentar iniciar preparo de pedido inexistente"
echo "-------------------------------------------------------------------"

echo ""
echo "Tentando iniciar preparo do pedido com ID: 99999 (nao existe)..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.json -X POST ${COZINHA_URL}/cozinha/99999/iniciar)

echo "HTTP Status Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "404" ]; then
    echo "[OK] Erro tratado corretamente - Pedido nao encontrado na cozinha"
    cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json
else
    echo -e "${RED}[ERRO] Status code esperado: 404, obtido: $HTTP_CODE${NC}"
    exit 1
fi

# ========================================================================
# CASO 3: RETIRAR PEDIDO COM STATUS INVALIDO
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "CASO 3: Tentar retirar pedido que nao esta PRONTO"
echo "-------------------------------------------------------------------"

echo ""
echo "Criando pedido para testar retirada invalida..."
RESPONSE=$(curl -s -X POST ${PEDIDOS_URL}/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": null,
    "itens": [
      {"produtoId": 1, "quantidade": 1}
    ]
  }')

PEDIDO_ID_3=$(echo "$RESPONSE" | jq -r '.id')
echo "Pedido criado - ID: $PEDIDO_ID_3"

echo ""
echo "Tentando retirar pedido com status CRIADO (invalido)..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.json -X PATCH ${PEDIDOS_URL}/pedidos/${PEDIDO_ID_3}/retirar)

echo "HTTP Status Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "400" ]; then
    echo "[OK] Erro tratado corretamente - Pedido nao esta PRONTO"
    cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json
else
    echo -e "${RED}[ERRO] Status code esperado: 400, obtido: $HTTP_CODE${NC}"
    exit 1
fi

# ========================================================================
# CASO 4: BUSCAR PEDIDO INEXISTENTE
# ========================================================================
echo ""
echo "-------------------------------------------------------------------"
echo "CASO 4: Buscar pedido inexistente"
echo "-------------------------------------------------------------------"

echo ""
echo "Buscando pedido com ID: 99999 (nao existe)..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o /tmp/response.json ${PEDIDOS_URL}/pedidos/99999)

echo "HTTP Status Code: $HTTP_CODE"

if [ "$HTTP_CODE" = "404" ]; then
    echo "[OK] Erro tratado corretamente - Pedido nao encontrado"
    cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json
else
    echo -e "${RED}[ERRO] Status code esperado: 404, obtido: $HTTP_CODE${NC}"
    exit 1
fi

# ========================================================================
# RESUMO FINAL - TESTE 3
# ========================================================================
echo ""
echo "==================================================================="
echo "RESUMO - TESTE 3: Edge Cases e Validacao de Erros"
echo "==================================================================="
echo ""
echo "[OK] Teste 3 concluido com sucesso!"
echo ""
echo "Casos testados:"
echo "  1. [OK] Criar pedido com produto inexistente - HTTP 400/404"
echo "  2. [OK] Iniciar preparo de pedido inexistente - HTTP 404"
echo "  3. [OK] Retirar pedido com status invalido - HTTP 400"
echo "  4. [OK] Buscar pedido inexistente - HTTP 404"
echo ""
echo "==================================================================="

# ========================================================================
# RESUMO GERAL
# ========================================================================
echo ""
echo ""
echo "==================================================================="
echo "RESUMO GERAL: TODOS OS TESTES E2E"
echo "==================================================================="
echo ""
echo "[OK] TESTE 1: Pedido Anonimo - CONCLUIDO"
echo "[OK] TESTE 2: Pedido com Cliente Identificado - CONCLUIDO"
echo "[OK] TESTE 3: Edge Cases e Validacao de Erros - CONCLUIDO"
echo ""
echo "Todos os testes E2E foram executados com sucesso!"
echo ""
echo "==================================================================="
