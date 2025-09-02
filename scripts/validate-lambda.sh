#!/bin/bash

set -e

echo "✅ VALIDANDO LAMBDA DE AUTENTICAÇÃO"
echo "==================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    ((ERRORS++))
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
    ((WARNINGS++))
}

print_info() {
    echo -e "ℹ️  $1"
}

# Verificar se AWS CLI está configurado
echo "🔍 VERIFICANDO CONFIGURAÇÃO AWS"
echo "-------------------------------"

if ! command -v aws &> /dev/null; then
    print_error "AWS CLI não encontrado. Instale primeiro."
    exit 1
else
    print_success "AWS CLI encontrado"
fi

# Verificar credenciais AWS
if ! aws sts get-caller-identity &> /dev/null; then
    print_error "Credenciais AWS não configuradas ou inválidas"
    exit 1
else
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    print_success "Credenciais AWS válidas (Account: $ACCOUNT_ID)"
fi

echo ""

# Verificar se a Lambda existe
echo "🔍 VERIFICANDO LAMBDA FUNCTION"
echo "------------------------------"

LAMBDA_NAME="lanchonete-auth"
if aws lambda get-function --function-name "$LAMBDA_NAME" &> /dev/null; then
    print_success "Lambda '$LAMBDA_NAME' encontrada"
    
    # Obter informações da Lambda
    LAMBDA_INFO=$(aws lambda get-function --function-name "$LAMBDA_NAME")
    RUNTIME=$(echo "$LAMBDA_INFO" | jq -r '.Configuration.Runtime')
    MEMORY=$(echo "$LAMBDA_INFO" | jq -r '.Configuration.MemorySize')
    TIMEOUT=$(echo "$LAMBDA_INFO" | jq -r '.Configuration.Timeout')
    HANDLER=$(echo "$LAMBDA_INFO" | jq -r '.Configuration.Handler')
    
    print_info "Runtime: $RUNTIME"
    print_info "Memory: ${MEMORY}MB"
    print_info "Timeout: ${TIMEOUT}s"
    print_info "Handler: $HANDLER"
    
    # Validar configurações
    if [[ "$RUNTIME" == "java17" ]]; then
        print_success "Runtime Java 17 correto"
    else
        print_error "Runtime incorreto. Esperado: java17, Atual: $RUNTIME"
    fi
    
    if [[ "$MEMORY" -ge "512" ]]; then
        print_success "Memory configurada adequadamente (${MEMORY}MB)"
    else
        print_warning "Memory baixa (${MEMORY}MB). Recomendado: 512MB+"
    fi
    
    if [[ "$HANDLER" == "br.com.lanchonete.auth.AuthHandler::handleRequest" ]]; then
        print_success "Handler correto"
    else
        print_error "Handler incorreto. Esperado: br.com.lanchonete.auth.AuthHandler::handleRequest"
    fi
    
else
    print_error "Lambda '$LAMBDA_NAME' não encontrada"
fi

echo ""

# Verificar variáveis de ambiente
echo "🔍 VERIFICANDO VARIÁVEIS DE AMBIENTE"
echo "------------------------------------"

if aws lambda get-function --function-name "$LAMBDA_NAME" &> /dev/null; then
    ENV_VARS=$(aws lambda get-function-configuration --function-name "$LAMBDA_NAME" | jq -r '.Environment.Variables // {}')
    
    # Verificar cada variável obrigatória
    REQUIRED_VARS=("DATABASE_URL" "DB_USERNAME" "DB_PASSWORD" "JWT_SECRET")
    
    for VAR in "${REQUIRED_VARS[@]}"; do
        if echo "$ENV_VARS" | jq -e "has(\"$VAR\")" > /dev/null; then
            print_success "Variável $VAR configurada"
        else
            print_error "Variável $VAR não encontrada"
        fi
    done
    
    # Verificar se JWT_SECRET tem tamanho adequado
    JWT_SECRET_LENGTH=$(echo "$ENV_VARS" | jq -r '.JWT_SECRET // ""' | wc -c)
    if [[ "$JWT_SECRET_LENGTH" -gt "32" ]]; then
        print_success "JWT_SECRET tem tamanho adequado"
    else
        print_error "JWT_SECRET muito curto (${JWT_SECRET_LENGTH} chars). Mínimo: 32 chars"
    fi
fi

echo ""

# Verificar logs da Lambda
echo "🔍 VERIFICANDO LOGS DA LAMBDA"
echo "-----------------------------"

LOG_GROUP="/aws/lambda/$LAMBDA_NAME"
if aws logs describe-log-groups --log-group-name-prefix "$LOG_GROUP" | jq -e '.logGroups | length > 0' > /dev/null; then
    print_success "Log group '$LOG_GROUP' existe"
    
    # Verificar logs recentes (últimos 5 minutos)
    START_TIME=$(($(date +%s) * 1000 - 300000))
    
    RECENT_LOGS=$(aws logs filter-log-events \
        --log-group-name "$LOG_GROUP" \
        --start-time "$START_TIME" \
        --query 'events[0:5].message' \
        --output text 2>/dev/null || echo "")
    
    if [[ -n "$RECENT_LOGS" ]]; then
        print_success "Logs recentes encontrados"
        print_info "Últimas mensagens:"
        echo "$RECENT_LOGS" | head -3 | sed 's/^/   /'
    else
        print_warning "Nenhum log recente encontrado (últimos 5 min)"
    fi
else
    print_error "Log group '$LOG_GROUP' não encontrado"
fi

echo ""

# Verificar conectividade com RDS (se possível)
echo "🔍 VERIFICANDO CONECTIVIDADE COM RDS"
echo "------------------------------------"

# Tentar obter endpoint do RDS
RDS_ENDPOINT=""
if aws rds describe-db-instances --query 'DBInstances[?DBName==`lanchonete`].Endpoint.Address' --output text &> /dev/null; then
    RDS_ENDPOINT=$(aws rds describe-db-instances --query 'DBInstances[?DBName==`lanchonete`].Endpoint.Address' --output text)
    if [[ -n "$RDS_ENDPOINT" && "$RDS_ENDPOINT" != "None" ]]; then
        print_success "RDS endpoint encontrado: $RDS_ENDPOINT"
    else
        print_warning "RDS não encontrado ou não acessível"
    fi
else
    print_warning "Não foi possível verificar RDS"
fi

echo ""

# Verificar API Gateway (se existir)
echo "🔍 VERIFICANDO API GATEWAY"
echo "-------------------------"

# Procurar API Gateway com nome específico
API_ID=$(aws apigateway get-rest-apis --query 'items[?name==`lanchonete-api`].id' --output text)
if [[ -n "$API_ID" && "$API_ID" != "None" ]]; then
    print_success "API Gateway 'lanchonete-api' encontrada (ID: $API_ID)"
    
    # Verificar estágio de deployment
    STAGES=$(aws apigateway get-stages --rest-api-id "$API_ID" --query 'item[].stageName' --output text)
    if [[ -n "$STAGES" ]]; then
        print_success "Estágios deployment: $STAGES"
        
        # Construir URL da API
        REGION=$(aws configure get region || echo "us-east-1")
        API_URL="https://${API_ID}.execute-api.${REGION}.amazonaws.com/prod"
        print_info "URL da API: $API_URL"
    else
        print_warning "Nenhum estágio de deployment encontrado"
    fi
else
    print_warning "API Gateway 'lanchonete-api' não encontrada"
fi

echo ""

# Executar teste simples da Lambda (se possível)
echo "🔍 EXECUTANDO TESTE SIMPLES"
echo "---------------------------"

TEST_PAYLOAD='{"body": "{\"authType\": \"anonimo\"}"}'
if aws lambda invoke \
    --function-name "$LAMBDA_NAME" \
    --payload "$TEST_PAYLOAD" \
    --cli-binary-format raw-in-base64-out \
    /tmp/lambda-test-response.json &> /dev/null; then
    
    RESPONSE_STATUS=$(cat /tmp/lambda-test-response.json | jq -r '.statusCode // "N/A"')
    if [[ "$RESPONSE_STATUS" == "200" ]]; then
        print_success "Teste simples executado com sucesso (Status: $RESPONSE_STATUS)"
    else
        print_warning "Teste executado mas retornou status: $RESPONSE_STATUS"
        print_info "Response: $(cat /tmp/lambda-test-response.json)"
    fi
    
    rm -f /tmp/lambda-test-response.json
else
    print_error "Falha ao executar teste simples na Lambda"
fi

echo ""

# Resumo final
echo "📊 RESUMO DA VALIDAÇÃO"
echo "======================"

if [[ $ERRORS -eq 0 ]]; then
    print_success "Todos os checks críticos passaram!"
    
    if [[ $WARNINGS -eq 0 ]]; then
        echo ""
        echo "🎉 LAMBDA DE AUTENTICAÇÃO TOTALMENTE VALIDADA!"
        echo "✅ Pronta para uso em produção"
    else
        echo ""
        echo "⚠️  Lambda funcional mas com $WARNINGS warning(s)"
        echo "💡 Verifique os warnings acima para otimizações"
    fi
else
    print_error "$ERRORS erro(s) crítico(s) encontrado(s)"
    print_error "Lambda NÃO está pronta para uso"
    echo ""
    echo "🔧 CORRIJA OS ERROS ANTES DE PROSSEGUIR"
    exit 1
fi

# Próximos passos
echo ""
echo "🚀 PRÓXIMOS PASSOS SUGERIDOS:"
echo "1. Execute: ./scripts/test-lambda-auth.sh"
echo "2. Teste end-to-end com API Gateway"
echo "3. Valide JWT tokens gerados"
echo "4. Configure monitoramento CloudWatch"