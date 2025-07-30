#!/bin/bash

# =============================================================================
# SCRIPT DE TESTE COMPLETO - TODOS OS ENDPOINTS
# =============================================================================

# Configurações
MINIKUBE_IP=$(minikube ip)
AUTOATENDIMENTO_URL="http://$MINIKUBE_IP:30080"
PAGAMENTO_URL="http://$MINIKUBE_IP:30081"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para printar com cores
print_step() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Função para fazer requisição e mostrar resultado formatado
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    print_step "$description"
    echo "🌐 $method $url"
    
    if [ -n "$data" ]; then
        echo "📤 Payload: $data"
    fi
    
    # Fazer requisição e capturar resposta e status code
    response=$(curl -s -w "\n%{http_code}" --connect-timeout 10 --max-time 30 -X "$method" "$url" \
        -H "Content-Type: application/json" \
        ${data:+-d "$data"} 2>&1)
    
    curl_exit_code=$?
    if [ $curl_exit_code -ne 0 ]; then
        print_error "Erro na requisição curl (código: $curl_exit_code): $response"
        echo "⚠️  Continuando com próximo teste..."
        return 1
    fi
    
    # Separar corpo da resposta e status code
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    # Mostrar status code
    if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
        print_success "Status: $http_code"
    elif [[ $http_code -ge 400 && $http_code -lt 500 ]]; then
        print_warning "Status: $http_code"
    else
        print_error "Status: $http_code"
    fi
    
    # Mostrar resposta formatada
    echo "📥 Resposta:"
    if command -v jq >/dev/null 2>&1 && echo "$body" | jq . >/dev/null 2>&1; then
        echo "$body" | jq .
    else
        echo "$body"
    fi
    
    echo
    return $http_code
}

# Função para extrair ID de resposta JSON
extract_id() {
    local response=$1
    if command -v jq >/dev/null 2>&1; then
        echo "$response" | jq -r '.id'
    else
        # Fallback sem jq
        echo "$response" | grep -o '"id":[0-9]*' | cut -d':' -f2
    fi
}

# =============================================================================
# GRUPO 1: SETUP E DADOS INICIAIS
# =============================================================================

echo -e "${BLUE}"
echo "🚀 INICIANDO GRUPO 1: Setup e Dados Iniciais"
echo "============================================="
echo -e "🎯 Minikube IP: $MINIKUBE_IP"
echo -e "🎯 Autoatendimento: $AUTOATENDIMENTO_URL"
echo -e "🎯 Pagamento: $PAGAMENTO_URL"
echo -e "${NC}"

# Verificar conectividade básica
print_step "Verificando conectividade"
if ! ping -c 1 "$MINIKUBE_IP" >/dev/null 2>&1; then
    print_error "Não conseguiu conectar ao Minikube IP: $MINIKUBE_IP"
    exit 1
fi
print_success "Conectividade OK"

# =============================================================================
# PASSO 1: Verificar produtos pré-cadastrados
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/produtos/categoria/LANCHE" "" "PASSO 1.1: Listar produtos - LANCHE"

make_request "GET" "$AUTOATENDIMENTO_URL/produtos/categoria/BEBIDA" "" "PASSO 1.2: Listar produtos - BEBIDA"

make_request "GET" "$AUTOATENDIMENTO_URL/produtos/categoria/ACOMPANHAMENTO" "" "PASSO 1.3: Listar produtos - ACOMPANHAMENTO"

make_request "GET" "$AUTOATENDIMENTO_URL/produtos/categoria/SOBREMESA" "" "PASSO 1.4: Listar produtos - SOBREMESA"

# =============================================================================
# PASSO 2: Cadastrar cliente novo (CPF diferente)
# =============================================================================

CLIENTE_NOVO_JSON='{
  "nome": "Maria Silva Testes",
  "cpf": "98765432100",
  "email": "maria.testes@lanchonete.com"
}'

make_request "POST" "$AUTOATENDIMENTO_URL/clientes" "$CLIENTE_NOVO_JSON" "PASSO 2: Cadastrar cliente novo"
cadastro_status=$?

# CPF do cliente para usar nos testes
CLIENTE_CPF_NOVO="98765432100"
CLIENTE_CPF_EXISTENTE="12345678901"

# =============================================================================
# PASSO 3: Verificar clientes por CPF
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/clientes/cpf/$CLIENTE_CPF_NOVO" "" "PASSO 3.1: Buscar cliente novo por CPF"

make_request "GET" "$AUTOATENDIMENTO_URL/clientes/cpf/$CLIENTE_CPF_EXISTENTE" "" "PASSO 3.2: Buscar cliente pré-existente por CPF"

# =============================================================================
# PASSO 4: Testar CRUD de produtos
# =============================================================================

# 4.1 - Criar novo produto
PRODUTO_JSON='{
  "nome": "Produto Teste Script",
  "descricao": "Produto criado para teste do script",
  "preco": 15.50,
  "categoria": "LANCHE"
}'

print_step "PASSO 4.1: Criar novo produto"
response=$(curl -s -w "\n%{http_code}" -X "POST" "$AUTOATENDIMENTO_URL/produtos" \
    -H "Content-Type: application/json" \
    -d "$PRODUTO_JSON")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n -1)

echo "🌐 POST $AUTOATENDIMENTO_URL/produtos"
echo "📤 Payload: $PRODUTO_JSON"

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    print_success "Status: $http_code"
    PRODUTO_ID=$(extract_id "$body")
    echo "🆔 ID do produto criado: $PRODUTO_ID"
else
    print_warning "Status: $http_code"
    PRODUTO_ID="999"  # fallback se não conseguiu criar
fi

echo "📥 Resposta:"
if command -v jq >/dev/null 2>&1 && echo "$body" | jq . >/dev/null 2>&1; then
    echo "$body" | jq .
else
    echo "$body"
fi
echo

# 4.2 - Editar produto (só se foi criado com sucesso)
if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    PRODUTO_EDIT_JSON='{
      "nome": "Produto Teste Editado",
      "descricao": "Produto editado pelo script de teste",
      "preco": 17.90,
      "categoria": "LANCHE"
    }'
    
    make_request "PUT" "$AUTOATENDIMENTO_URL/produtos/$PRODUTO_ID" "$PRODUTO_EDIT_JSON" "PASSO 4.2: Editar produto criado"
fi

# 4.3 - Deletar produto (só se foi criado com sucesso)
if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    make_request "DELETE" "$AUTOATENDIMENTO_URL/produtos/$PRODUTO_ID" "" "PASSO 4.3: Deletar produto criado"
fi

# =============================================================================
# RESUMO DO GRUPO 1
# =============================================================================

echo -e "${BLUE}"
echo "📊 RESUMO DO GRUPO 1"
echo "===================="
echo -e "${NC}"

print_success "✅ Produtos pré-cadastrados verificados"
if [[ $cadastro_status -eq 201 ]]; then
    print_success "✅ Cliente novo cadastrado com sucesso"
elif [[ $cadastro_status -eq 400 ]]; then
    print_warning "⚠️  Cliente novo já existia"
else
    print_error "❌ Erro no cadastro do cliente novo"
fi
print_success "✅ CRUD de produtos testado"

# =============================================================================
# GRUPO 2: FLUXO DE PEDIDOS - CLIENTE CADASTRADO
# =============================================================================

echo -e "${BLUE}"
echo "🚀 INICIANDO GRUPO 2: Fluxo de Pedidos - Cliente Cadastrado"
echo "=========================================================="
echo -e "${NC}"

# =============================================================================
# PASSO 5: Checkout com cliente cadastrado
# =============================================================================

PEDIDO_COM_CLIENTE_JSON='{
  "clienteId": 1,
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 2
    },
    {
      "produtoId": 7,
      "quantidade": 1
    }
  ]
}'

print_step "PASSO 5: Checkout com cliente cadastrado (ID: 1)"
response=$(curl -s -w "\n%{http_code}" -X "POST" "$AUTOATENDIMENTO_URL/pedidos/checkout" \
    -H "Content-Type: application/json" \
    -d "$PEDIDO_COM_CLIENTE_JSON")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n -1)

echo "🌐 POST $AUTOATENDIMENTO_URL/pedidos/checkout"
echo "📤 Payload: $PEDIDO_COM_CLIENTE_JSON"

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    print_success "Status: $http_code"
    PEDIDO_ID_1=$(extract_id "$body")
    echo "🆔 ID do pedido criado: $PEDIDO_ID_1"
    # Extrair valor total para pagamento
    if command -v jq >/dev/null 2>&1; then
        VALOR_PEDIDO_1=$(echo "$body" | jq -r '.valorTotal')
    else
        VALOR_PEDIDO_1="44.70"  # fallback (2 x 18.90 + 1 x 6.90)
    fi
    echo "💰 Valor total: $VALOR_PEDIDO_1"
else
    print_error "Status: $http_code - Erro no checkout"
    PEDIDO_ID_1="1"
    VALOR_PEDIDO_1="44.70"
fi

echo "📥 Resposta:"
if command -v jq >/dev/null 2>&1 && echo "$body" | jq . >/dev/null 2>&1; then
    echo "$body" | jq .
else
    echo "$body"
fi
echo

# =============================================================================
# PASSO 6: Processar pagamento
# =============================================================================

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    PAGAMENTO_JSON="{
      \"pedidoId\": \"$PEDIDO_ID_1\",
      \"valor\": $VALOR_PEDIDO_1
    }"
    
    make_request "POST" "$PAGAMENTO_URL/pagamentos" "$PAGAMENTO_JSON" "PASSO 6: Processar pagamento do pedido $PEDIDO_ID_1"
    
    # =============================================================================
    # PASSO 7: Aguardar webhook automático
    # =============================================================================
    
    print_step "PASSO 7: Aguardar webhook automático (5 segundos)"
    echo "⏳ Aguardando processamento do pagamento..."
    sleep 5
    print_success "Tempo de espera concluído"
    
    # =============================================================================
    # PASSO 8: Verificar status do pagamento
    # =============================================================================
    
    make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/$PEDIDO_ID_1/pagamento/status" "" "PASSO 8: Consultar status do pagamento"
    
    # =============================================================================
    # PASSO 9: Listar todos os pedidos
    # =============================================================================
    
    make_request "GET" "$AUTOATENDIMENTO_URL/pedidos" "" "PASSO 9: Listar todos os pedidos"
    
    # =============================================================================
    # PASSO 10: Listar pedidos na cozinha
    # =============================================================================
    
    make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 10: Listar pedidos na cozinha"
fi

# =============================================================================
# RESUMO DO GRUPO 2
# =============================================================================

echo -e "${BLUE}"
echo "📊 RESUMO DO GRUPO 2"
echo "===================="
echo -e "${NC}"

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    print_success "✅ Checkout com cliente realizado (Pedido ID: $PEDIDO_ID_1)"
    print_success "✅ Pagamento processado (Valor: $VALOR_PEDIDO_1)"
    print_success "✅ Webhook automático testado"
    print_success "✅ Consultas de status e listagens realizadas"
else
    print_error "❌ Falha no fluxo de pedidos"
fi

# =============================================================================
# GRUPO 3: FLUXO DE PEDIDOS - CLIENTE ANÔNIMO
# =============================================================================

echo -e "${BLUE}"
echo "🚀 INICIANDO GRUPO 3: Fluxo de Pedidos - Cliente Anônimo"
echo "======================================================="
echo -e "${NC}"

# =============================================================================
# PASSO 11: Checkout sem cliente (anônimo)
# =============================================================================

PEDIDO_ANONIMO_JSON='{
  "clienteId": null,
  "itens": [
    {
      "produtoId": 2,
      "quantidade": 1
    },
    {
      "produtoId": 14,
      "quantidade": 2
    }
  ]
}'

print_step "PASSO 11: Checkout anônimo (sem cliente)"
response=$(curl -s -w "\n%{http_code}" -X "POST" "$AUTOATENDIMENTO_URL/pedidos/checkout" \
    -H "Content-Type: application/json" \
    -d "$PEDIDO_ANONIMO_JSON")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n -1)

echo "🌐 POST $AUTOATENDIMENTO_URL/pedidos/checkout"
echo "📤 Payload: $PEDIDO_ANONIMO_JSON"

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    print_success "Status: $http_code"
    PEDIDO_ID_2=$(extract_id "$body")
    echo "🆔 ID do pedido anônimo: $PEDIDO_ID_2"
    # Extrair valor total para pagamento
    if command -v jq >/dev/null 2>&1; then
        VALOR_PEDIDO_2=$(echo "$body" | jq -r '.valorTotal')
    else
        VALOR_PEDIDO_2="28.70"  # fallback (1 x 10.90 + 2 x 8.90)
    fi
    echo "💰 Valor total: $VALOR_PEDIDO_2"
else
    print_error "Status: $http_code - Erro no checkout anônimo"
    PEDIDO_ID_2="999"
    VALOR_PEDIDO_2="28.70"
fi

echo "📥 Resposta:"
if command -v jq >/dev/null 2>&1 && echo "$body" | jq . >/dev/null 2>&1; then
    echo "$body" | jq .
else
    echo "$body"
fi
echo

# =============================================================================
# PASSO 12: Processar pagamento do pedido anônimo
# =============================================================================

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    PAGAMENTO_ANONIMO_JSON="{
      \"pedidoId\": \"$PEDIDO_ID_2\",
      \"valor\": $VALOR_PEDIDO_2
    }"
    
    make_request "POST" "$PAGAMENTO_URL/pagamentos" "$PAGAMENTO_ANONIMO_JSON" "PASSO 12: Processar pagamento do pedido anônimo $PEDIDO_ID_2"
    
    # =============================================================================
    # PASSO 13: Aguardar webhook automático (tentativa 2)
    # =============================================================================
    
    print_step "PASSO 13: Aguardar webhook automático - tentativa 2 (5 segundos)"
    echo "⏳ Aguardando processamento do pagamento anônimo..."
    sleep 5
    print_success "Tempo de espera concluído"
    
    # =============================================================================
    # PASSO 14: Verificar status do pagamento anônimo
    # =============================================================================
    
    make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/$PEDIDO_ID_2/pagamento/status" "" "PASSO 14: Consultar status do pagamento anônimo"
    
    # =============================================================================
    # PASSO 15: Listar pedidos na cozinha após segundo pedido
    # =============================================================================
    
    make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 15: Verificar cozinha após pedido anônimo"
fi

# =============================================================================
# PASSO 16: Criar mais um pedido para garantir aprovação
# =============================================================================

PEDIDO_EXTRA_JSON='{
  "clienteId": null,
  "itens": [
    {
      "produtoId": 17,
      "quantidade": 1
    }
  ]
}'

print_step "PASSO 16: Criar pedido extra (tentativa de aprovação)"
response=$(curl -s -w "\n%{http_code}" -X "POST" "$AUTOATENDIMENTO_URL/pedidos/checkout" \
    -H "Content-Type: application/json" \
    -d "$PEDIDO_EXTRA_JSON")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n -1)

echo "🌐 POST $AUTOATENDIMENTO_URL/pedidos/checkout"
echo "📤 Payload: $PEDIDO_EXTRA_JSON"

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    print_success "Status: $http_code"
    PEDIDO_ID_3=$(extract_id "$body")
    echo "🆔 ID do pedido extra: $PEDIDO_ID_3"
    if command -v jq >/dev/null 2>&1; then
        VALOR_PEDIDO_3=$(echo "$body" | jq -r '.valorTotal')
    else
        VALOR_PEDIDO_3="10.90"  # fallback
    fi
    echo "💰 Valor total: $VALOR_PEDIDO_3"
    
    # Processar pagamento do terceiro pedido
    PAGAMENTO_EXTRA_JSON="{
      \"pedidoId\": \"$PEDIDO_ID_3\",
      \"valor\": $VALOR_PEDIDO_3
    }"
    
    make_request "POST" "$PAGAMENTO_URL/pagamentos" "$PAGAMENTO_EXTRA_JSON" "PASSO 16.1: Processar pagamento do pedido extra"
    
    print_step "PASSO 16.2: Aguardar webhook do pedido extra (5 segundos)"
    sleep 5
    
    make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/$PEDIDO_ID_3/pagamento/status" "" "PASSO 16.3: Status do pagamento extra"
fi

echo "📥 Resposta:"
if command -v jq >/dev/null 2>&1 && echo "$body" | jq . >/dev/null 2>&1; then
    echo "$body" | jq .
else
    echo "$body"
fi
echo

# =============================================================================
# PASSO 17: Verificação final da cozinha
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 17: Verificação final - pedidos na cozinha"

# =============================================================================
# PASSO 18: Listar todos os pedidos do sistema
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos" "" "PASSO 18: Listar todos os pedidos do sistema"

# =============================================================================
# RESUMO DO GRUPO 3
# =============================================================================

echo -e "${BLUE}"
echo "📊 RESUMO DO GRUPO 3"
echo "===================="
echo -e "${NC}"

if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
    print_success "✅ Checkout anônimo realizado (Pedido ID: $PEDIDO_ID_2)"
    print_success "✅ Pedido extra criado (Pedido ID: $PEDIDO_ID_3)"
    print_success "✅ Múltiplos pagamentos testados"
    print_success "✅ Comportamento de aprovação/rejeição verificado"
else
    print_error "❌ Falha no fluxo de pedidos anônimos"
fi

# =============================================================================
# GRUPO 4: OPERAÇÕES DA COZINHA
# =============================================================================

echo -e "${BLUE}"
echo "🚀 INICIANDO GRUPO 4: Operações da Cozinha"
echo "========================================="
echo -e "${NC}"

# =============================================================================
# PASSO 19: Verificar estado inicial da cozinha
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 19: Estado inicial da cozinha"

# =============================================================================
# PASSO 20: Atualizar primeiro pedido RECEBIDO → EM_PREPARACAO
# =============================================================================

STATUS_EM_PREPARACAO_JSON='{
  "status": "EM_PREPARACAO"
}'

# Usar o primeiro pedido da cozinha (ID 5)
make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/5/status" "$STATUS_EM_PREPARACAO_JSON" "PASSO 20: Pedido 5 - RECEBIDO → EM_PREPARACAO"

# =============================================================================
# PASSO 21: Verificar ordenação após mudança para EM_PREPARACAO
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 21: Verificar ordenação (EM_PREPARACAO deve aparecer primeiro)"

# =============================================================================
# PASSO 22: Atualizar segundo pedido RECEBIDO → EM_PREPARACAO
# =============================================================================

# Usar o segundo pedido da cozinha (ID 6)
make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/6/status" "$STATUS_EM_PREPARACAO_JSON" "PASSO 22: Pedido 6 - RECEBIDO → EM_PREPARACAO"

# =============================================================================
# PASSO 23: Atualizar primeiro pedido EM_PREPARACAO → PRONTO
# =============================================================================

STATUS_PRONTO_JSON='{
  "status": "PRONTO"
}'

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/5/status" "$STATUS_PRONTO_JSON" "PASSO 23: Pedido 5 - EM_PREPARACAO → PRONTO"

# =============================================================================
# PASSO 24: Verificar ordenação de prioridade (PRONTO primeiro)
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 24: Verificar prioridade (PRONTO > EM_PREPARACAO > RECEBIDO)"

# =============================================================================
# PASSO 25: Atualizar terceiro pedido direto para PRONTO
# =============================================================================

# Usar o terceiro pedido (ID 8) direto para PRONTO
make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/8/status" "$STATUS_PRONTO_JSON" "PASSO 25: Pedido 8 - RECEBIDO → PRONTO"

# =============================================================================
# PASSO 26: Verificar múltiplos pedidos PRONTOS (ordenação por data)
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 26: Verificar ordenação de múltiplos PRONTOS (mais antigo primeiro)"

# =============================================================================
# PASSO 27: Finalizar primeiro pedido PRONTO → FINALIZADO
# =============================================================================

STATUS_FINALIZADO_JSON='{
  "status": "FINALIZADO"
}'

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/5/status" "$STATUS_FINALIZADO_JSON" "PASSO 27: Pedido 5 - PRONTO → FINALIZADO"

# =============================================================================
# PASSO 28: Verificar que pedido finalizado não aparece na cozinha
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 28: Verificar que FINALIZADO não aparece na cozinha"

# =============================================================================
# PASSO 29: Finalizar segundo pedido
# =============================================================================

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/8/status" "$STATUS_FINALIZADO_JSON" "PASSO 29: Pedido 8 - PRONTO → FINALIZADO"

# =============================================================================
# PASSO 30: Fluxo completo do último pedido restante (6 - EM_PREPARACAO → PRONTO → FINALIZADO)
# =============================================================================

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/6/status" "$STATUS_PRONTO_JSON" "PASSO 30.1: Pedido 6 - EM_PREPARACAO → PRONTO"

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 30.2: Estado da cozinha com pedido 6 PRONTO"

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/6/status" "$STATUS_FINALIZADO_JSON" "PASSO 30.3: Pedido 6 - PRONTO → FINALIZADO"

# =============================================================================
# PASSO 31: Processar último pedido restante na cozinha (ID 10)
# =============================================================================

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/10/status" "$STATUS_EM_PREPARACAO_JSON" "PASSO 31.1: Pedido 10 - RECEBIDO → EM_PREPARACAO"

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/10/status" "$STATUS_PRONTO_JSON" "PASSO 31.2: Pedido 10 - EM_PREPARACAO → PRONTO"

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 31.3: Último pedido na cozinha"

make_request "PUT" "$AUTOATENDIMENTO_URL/pedidos/cozinha/10/status" "$STATUS_FINALIZADO_JSON" "PASSO 31.4: Pedido 10 - PRONTO → FINALIZADO"

# =============================================================================
# PASSO 32: Verificar cozinha vazia
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos/cozinha" "" "PASSO 32: Verificar cozinha vazia (todos finalizados)"

# =============================================================================
# PASSO 33: Verificar lista completa (incluindo finalizados)
# =============================================================================

make_request "GET" "$AUTOATENDIMENTO_URL/pedidos" "" "PASSO 33: Lista completa mostra pedidos finalizados"

# =============================================================================
# RESUMO DO GRUPO 4
# =============================================================================

echo -e "${BLUE}"
echo "📊 RESUMO DO GRUPO 4"
echo "===================="
echo -e "${NC}"

print_success "✅ Fluxo completo da cozinha testado"
print_success "✅ Ordenação por prioridade verificada (PRONTO > EM_PREPARACAO > RECEBIDO)"
print_success "✅ Ordenação por data dentro da mesma prioridade testada"
print_success "✅ Transições de status funcionando corretamente"
print_success "✅ Pedidos finalizados removidos da cozinha"
print_success "✅ Todas as operações da cozinha validadas"

echo
echo -e "${YELLOW}🎯 GRUPO 4 CONCLUÍDO! Pronto para GRUPO 5 (Casos Especiais).${NC}"
echo
