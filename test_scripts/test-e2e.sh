#!/bin/bash

# Script de teste: Realizar pedido e verificar status
# Testa fluxo completo: pedido -> pagamento -> atualização status

set -e

MINIKUBE_URL=$(minikube service pedidos-service-nodeport --url)

echo "==================================================================="
echo "TESTE 1: Pedido Anonimo"
echo "==================================================================="

# Criar pedido anônimo
echo ""
echo "Criando pedido anonimo..."
RESPONSE=$(curl -s -X POST ${MINIKUBE_URL}/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": null,
    "itens": [
      {"produtoId": 1, "quantidade": 1},
      {"produtoId": 4, "quantidade": 1},
      {"produtoId": 6, "quantidade": 1}
    ]
  }')

echo "$RESPONSE" | jq .

PEDIDO_ID=$(echo "$RESPONSE" | jq -r '.id')
echo ""
echo "Pedido ID: $PEDIDO_ID"

# Aguardar processamento
echo ""
echo "Aguardando processamento do pagamento (2 segundos)..."
sleep 2

# Verificar status
echo ""
echo "Verificando status do pedido..."
STATUS_RESPONSE=$(curl -s ${MINIKUBE_URL}/pedidos/${PEDIDO_ID})
echo "$STATUS_RESPONSE" | jq .

STATUS=$(echo "$STATUS_RESPONSE" | jq -r '.status')
echo ""
echo "Status final: $STATUS"

echo ""
echo "==================================================================="
echo "TESTE 2: Pedido com CPF"
echo "==================================================================="

# Criar pedido com CPF
echo ""
echo "Criando pedido com CPF 55555555555..."
RESPONSE2=$(curl -s -X POST ${MINIKUBE_URL}/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": "55555555555",
    "itens": [
      {"produtoId": 2, "quantidade": 2},
      {"produtoId": 7, "quantidade": 1}
    ]
  }')

echo "$RESPONSE2" | jq .

PEDIDO_ID2=$(echo "$RESPONSE2" | jq -r '.id')
echo ""
echo "Pedido ID: $PEDIDO_ID2"

# Aguardar processamento
echo ""
echo "Aguardando processamento do pagamento (2 segundos)..."
sleep 2

# Verificar status
echo ""
echo "Verificando status do pedido..."
STATUS_RESPONSE2=$(curl -s ${MINIKUBE_URL}/pedidos/${PEDIDO_ID2})
echo "$STATUS_RESPONSE2" | jq .

STATUS2=$(echo "$STATUS_RESPONSE2" | jq -r '.status')
echo ""
echo "Status final: $STATUS2"

echo ""
echo "==================================================================="
echo "Testes concluidos"
echo "==================================================================="
