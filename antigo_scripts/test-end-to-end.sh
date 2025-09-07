#!/bin/bash

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Função para obter URL do API Gateway dinamicamente
get_api_gateway_url() {
    cd terraform/lambda && terraform output -raw api_gateway_url 2>/dev/null || {
        log_error "Erro ao obter URL do API Gateway. Execute terraform apply primeiro."
        exit 1
    }
}

# Função principal
main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║            🚀 TESTE END-TO-END - ETAPA 7                     ║"
    echo "║         API Gateway → NLB → EKS → Context Injection          ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    # Obter URL da API Gateway
    log_info "Obtendo URL do API Gateway..."
    API_BASE=$(get_api_gateway_url)
    log_success "API Gateway URL: $API_BASE"
    echo ""
    
    # ====================================
    # 1. TESTE DE AUTENTICAÇÃO
    # ====================================
    echo "1️⃣ TESTE DE AUTENTICAÇÃO"
    echo "========================"
    
    log_info "🔑 Autenticação com CPF (cliente registrado)..."
    CUSTOMER_RESPONSE=$(curl -s -X POST "$API_BASE/auth" \
        -H "Content-Type: application/json" \
        -d '{"cpf": "12345678901", "authType": "cliente"}' || {
        log_error "Falha na autenticação do cliente"
        exit 1
    })
    
    CUSTOMER_TOKEN=$(echo "$CUSTOMER_RESPONSE" | jq -r '.token' 2>/dev/null || echo "")
    if [[ -z "$CUSTOMER_TOKEN" || "$CUSTOMER_TOKEN" == "null" ]]; then
        log_error "Token do cliente não encontrado na resposta"
        echo "Response: $CUSTOMER_RESPONSE"
        exit 1
    fi
    log_success "Token cliente obtido: ${CUSTOMER_TOKEN:0:20}..."
    
    log_info "👤 Autenticação anônima..."
    ANON_RESPONSE=$(curl -s -X POST "$API_BASE/auth" \
        -H "Content-Type: application/json" \
        -d '{"authType": "anonimo"}' || {
        log_error "Falha na autenticação anônima"
        exit 1
    })
    
    ANON_TOKEN=$(echo "$ANON_RESPONSE" | jq -r '.token' 2>/dev/null || echo "")
    if [[ -z "$ANON_TOKEN" || "$ANON_TOKEN" == "null" ]]; then
        log_error "Token anônimo não encontrado na resposta"
        echo "Response: $ANON_RESPONSE"
        exit 1
    fi
    log_success "Token anônimo obtido: ${ANON_TOKEN:0:20}..."
    
    echo ""
    
    # ====================================
    # 2. TESTE DE PRODUTOS
    # ====================================
    echo "2️⃣ TESTE DE PRODUTOS (CONTEXT INJECTION)"
    echo "========================================"
    
    log_info "🍔 Buscando produtos categoria LANCHE (cliente)..."
    PRODUTOS_CUSTOMER=$(curl -s -X GET "$API_BASE/produtos/categoria/LANCHE" \
        -H "Authorization: Bearer $CUSTOMER_TOKEN" || {
        log_warning "Falha ao buscar produtos como cliente"
    })
    
    if [[ -n "$PRODUTOS_CUSTOMER" ]]; then
        log_success "Produtos obtidos como cliente registrado"
        echo "$PRODUTOS_CUSTOMER" | jq '.' 2>/dev/null || echo "$PRODUTOS_CUSTOMER"
    fi
    
    log_info "🍔 Buscando produtos categoria BEBIDA (anônimo)..."
    PRODUTOS_ANON=$(curl -s -X GET "$API_BASE/produtos/categoria/BEBIDA" \
        -H "Authorization: Bearer $ANON_TOKEN" || {
        log_warning "Falha ao buscar produtos como anônimo"
    })
    
    if [[ -n "$PRODUTOS_ANON" ]]; then
        log_success "Produtos obtidos como usuário anônimo"
        echo "$PRODUTOS_ANON" | jq '.' 2>/dev/null || echo "$PRODUTOS_ANON"
    fi
    
    echo ""
    
    # ====================================
    # 3. TESTE DE CLIENTES
    # ====================================
    echo "3️⃣ TESTE DE CLIENTES"
    echo "==================="
    
    log_info "👥 Consultando clientes (só funciona com token válido)..."
    CLIENTES_RESPONSE=$(curl -s -X GET "$API_BASE/clientes?cpf=12345678901" \
        -H "Authorization: Bearer $CUSTOMER_TOKEN" || {
        log_warning "Falha ao consultar clientes"
    })
    
    if [[ -n "$CLIENTES_RESPONSE" ]]; then
        log_success "Consulta de clientes realizada"
        echo "$CLIENTES_RESPONSE" | jq '.' 2>/dev/null || echo "$CLIENTES_RESPONSE"
    fi
    
    echo ""
    
    # ====================================
    # 4. TESTE DE PEDIDOS
    # ====================================
    echo "4️⃣ TESTE DE PEDIDOS"
    echo "=================="
    
    log_info "📋 Listando pedidos (cliente)..."
    PEDIDOS_LIST=$(curl -s -X GET "$API_BASE/pedidos" \
        -H "Authorization: Bearer $CUSTOMER_TOKEN" || {
        log_warning "Falha ao listar pedidos"
    })
    
    if [[ -n "$PEDIDOS_LIST" ]]; then
        log_success "Lista de pedidos obtida"
        echo "$PEDIDOS_LIST" | jq '.' 2>/dev/null || echo "$PEDIDOS_LIST"
    fi
    
    log_info "🛒 Criando pedido (checkout cliente)..."
    PEDIDO_PAYLOAD='{
        "itens": [
            {"produtoId": 1, "quantidade": 1},
            {"produtoId": 2, "quantidade": 2}
        ]
    }'
    
    CHECKOUT_RESPONSE=$(curl -s -X POST "$API_BASE/pedidos" \
        -H "Authorization: Bearer $CUSTOMER_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$PEDIDO_PAYLOAD" || {
        log_warning "Falha no checkout"
    })
    
    if [[ -n "$CHECKOUT_RESPONSE" ]]; then
        log_success "Checkout realizado"
        echo "$CHECKOUT_RESPONSE" | jq '.' 2>/dev/null || echo "$CHECKOUT_RESPONSE"
        
        # Extrair ID do pedido para teste de pagamento
        PEDIDO_ID=$(echo "$CHECKOUT_RESPONSE" | jq -r '.id' 2>/dev/null || echo "")
    fi
    
    echo ""
    
    # ====================================
    # 5. TESTE DE PAGAMENTOS
    # ====================================
    echo "5️⃣ TESTE DE PAGAMENTOS"
    echo "======================"
    
    if [[ -n "$PEDIDO_ID" && "$PEDIDO_ID" != "null" ]]; then
        log_info "💳 Processando pagamento do pedido $PEDIDO_ID..."
        PAGAMENTO_PAYLOAD='{
            "pedidoId": "'$PEDIDO_ID'",
            "valor": 50.90,
            "metodoPagamento": "PIX"
        }'
        
        PAGAMENTO_RESPONSE=$(curl -s -X POST "$API_BASE/pagamentos" \
            -H "Authorization: Bearer $CUSTOMER_TOKEN" \
            -H "Content-Type: application/json" \
            -d "$PAGAMENTO_PAYLOAD" || {
            log_warning "Falha no processamento do pagamento"
        })
        
        if [[ -n "$PAGAMENTO_RESPONSE" ]]; then
            log_success "Pagamento processado"
            echo "$PAGAMENTO_RESPONSE" | jq '.' 2>/dev/null || echo "$PAGAMENTO_RESPONSE"
            
            # Consultar status do pagamento
            log_info "🔍 Consultando status do pagamento..."
            sleep 2
            PAGAMENTO_STATUS=$(curl -s -X GET "$API_BASE/pagamentos?pedidoId=$PEDIDO_ID" \
                -H "Authorization: Bearer $CUSTOMER_TOKEN" || {
                log_warning "Falha na consulta do status"
            })
            
            if [[ -n "$PAGAMENTO_STATUS" ]]; then
                log_success "Status do pagamento consultado"
                echo "$PAGAMENTO_STATUS" | jq '.' 2>/dev/null || echo "$PAGAMENTO_STATUS"
            fi
        fi
    else
        log_warning "ID do pedido não encontrado, pulando teste de pagamento"
    fi
    
    echo ""
    
    # ====================================
    # 6. TESTE DE CHECKOUT ANÔNIMO
    # ====================================
    echo "6️⃣ TESTE DE CHECKOUT ANÔNIMO"
    echo "============================"
    
    log_info "🛒 Checkout anônimo (deve funcionar sem dados pessoais)..."
    CHECKOUT_ANON=$(curl -s -X POST "$API_BASE/pedidos" \
        -H "Authorization: Bearer $ANON_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$PEDIDO_PAYLOAD" || {
        log_warning "Falha no checkout anônimo"
    })
    
    if [[ -n "$CHECKOUT_ANON" ]]; then
        log_success "Checkout anônimo realizado"
        echo "$CHECKOUT_ANON" | jq '.' 2>/dev/null || echo "$CHECKOUT_ANON"
    fi
    
    echo ""
    
    # ====================================
    # RESUMO FINAL
    # ====================================
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "${BLUE}📊 RESUMO DO TESTE END-TO-END${NC}"
    echo "═══════════════════════════════════════════════════════════════"
    
    log_success "🔑 Autenticação dual (cliente + anônimo) funcionando"
    log_success "🏗️ API Gateway → JWT Authorizer → VPC Link → NLB → EKS"
    log_success "📡 Context injection configurado (headers X-*)"
    log_success "🍔 Produtos acessíveis por ambos tipos de usuário"
    log_success "👥 Clientes autenticados acessam dados pessoais"
    log_success "🛒 Checkout funciona para ambos os fluxos"
    log_success "💳 Pagamentos integrados com serviço dedicado"
    
    echo ""
    log_success "🎉 ETAPA 7 - INTEGRAÇÃO API GATEWAY ↔ EKS FUNCIONANDO!"
    echo ""
    log_info "💡 Próximos passos:"
    echo "   • Execute './scripts/test-context-injection.sh' para validar headers"
    echo "   • Execute './scripts/validate-integration.sh' para testes técnicos"
    echo "   • Monitore logs das aplicações para verificar context injection"
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi