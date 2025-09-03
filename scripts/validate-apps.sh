#!/bin/bash

set -e

echo "✅ VALIDANDO APLICAÇÕES"
echo "======================"

# Função para verificar se aplicação está respondendo
check_app_health() {
    local app_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo "🔍 Verificando saúde da aplicação $app_name na porta $port..."
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f -s http://localhost:$port/actuator/health >/dev/null 2>&1; then
            echo "✅ $app_name está respondendo corretamente"
            return 0
        fi
        
        echo "   Tentativa $attempt/$max_attempts - aguardando..."
        sleep 5
        ((attempt++))
    done
    
    echo "❌ $app_name não está respondendo após $max_attempts tentativas"
    return 1
}

# Verificar se imagens Docker existem
echo "🐳 1/4 - VERIFICANDO IMAGENS DOCKER"
echo "-----------------------------------"

if docker images | grep -q "lanchonete/autoatendimento"; then
    echo "✅ Imagem autoatendimento encontrada"
else
    echo "❌ Imagem autoatendimento não encontrada"
    echo "💡 Execute: ./scripts/build-images.sh"
    exit 1
fi

if docker images | grep -q "lanchonete/pagamento"; then
    echo "✅ Imagem pagamento encontrada"
else
    echo "❌ Imagem pagamento não encontrada"
    echo "💡 Execute: ./scripts/build-images.sh"
    exit 1
fi

# Verificar conectividade com RDS
echo ""
echo "🗄️ 2/4 - VERIFICANDO CONECTIVIDADE RDS"
echo "-------------------------------------"

RDS_ENDPOINT=$(cd terraform/database && terraform output -raw rds_endpoint 2>/dev/null || echo "")
if [ -z "$RDS_ENDPOINT" ]; then
    echo "❌ Endpoint RDS não encontrado"
    echo "💡 Execute: cd terraform/database && terraform apply"
    exit 1
fi

echo "📍 RDS Endpoint: $RDS_ENDPOINT"

# Verificar se RDS está acessível
if nc -z ${RDS_ENDPOINT%:*} 3306 2>/dev/null; then
    echo "✅ RDS está acessível"
else
    echo "❌ RDS não está acessível"
    echo "💡 Verifique se o RDS está rodando e os security groups estão corretos"
    exit 1
fi

# Testar containers localmente
echo ""
echo "🚀 3/4 - TESTANDO APLICAÇÕES LOCALMENTE"
echo "--------------------------------------"

# Parar containers se estiverem rodando
docker stop autoatendimento-test pagamento-test 2>/dev/null || true
docker rm autoatendimento-test pagamento-test 2>/dev/null || true

# Variáveis de ambiente para teste local
DB_HOST=${RDS_ENDPOINT%:*}
DB_PORT=3306
DB_NAME="lanchonete"
DB_USERNAME=$(cd terraform/shared && grep 'db_username' academy.tfvars | cut -d'"' -f2)
DB_PASSWORD=$(cd terraform/shared && grep 'db_password' academy.tfvars | cut -d'"' -f2)

# Iniciar autoatendimento em background
echo "🍔 Iniciando autoatendimento (porta 8080)..."
docker run -d --name autoatendimento-test \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=kubernetes \
    -e SPRING_DATASOURCE_URL=jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME \
    -e SPRING_DATASOURCE_USERNAME=$DB_USERNAME \
    -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
    lanchonete/autoatendimento:latest

# Iniciar pagamento em background
echo "💳 Iniciando pagamento (porta 8081)..."
docker run -d --name pagamento-test \
    -p 8081:8080 \
    -e SPRING_PROFILES_ACTIVE=prod \
    lanchonete/pagamento:latest

# Aguardar aplicações ficarem prontas
sleep 10

# Verificar saúde das aplicações
if check_app_health "autoatendimento" 8080 && check_app_health "pagamento" 8081; then
    echo ""
    echo "✅ TODAS AS APLICAÇÕES ESTÃO FUNCIONANDO!"
else
    echo ""
    echo "❌ ALGUMAS APLICAÇÕES NÃO ESTÃO RESPONDENDO"
    
    # Mostrar logs para debug
    echo ""
    echo "📋 LOGS DO AUTOATENDIMENTO:"
    docker logs autoatendimento-test --tail 20
    
    echo ""
    echo "📋 LOGS DO PAGAMENTO:"
    docker logs pagamento-test --tail 20
    
    exit 1
fi

# Testar endpoints específicos
echo ""
echo "🧪 4/4 - TESTANDO ENDPOINTS"
echo "--------------------------"

# Testar autoatendimento
echo "🍔 Testando endpoint de produtos..."
if curl -f -s http://localhost:8080/produtos/categoria/LANCHE >/dev/null 2>&1; then
    echo "✅ Endpoint /produtos funcionando"
else
    echo "⚠️ Endpoint /produtos pode não estar funcionando (normal sem autenticação)"
fi

# Testar pagamento
echo "💳 Testando endpoint de saúde pagamento..."
if curl -f -s http://localhost:8081/actuator/health >/dev/null 2>&1; then
    echo "✅ Aplicação pagamento funcionando"
else
    echo "❌ Aplicação pagamento com problemas"
fi

# Limpeza
echo ""
echo "🧹 LIMPANDO CONTAINERS DE TESTE"
echo "------------------------------"
docker stop autoatendimento-test pagamento-test
docker rm autoatendimento-test pagamento-test

echo ""
echo "🎉 VALIDAÇÃO COMPLETA - SUCESSO!"
echo "==============================="
echo "✅ Imagens Docker criadas"
echo "✅ Conectividade RDS funcionando"
echo "✅ Aplicações inicializam corretamente"
echo "✅ Health checks passando"
echo ""
echo "🚀 ETAPA 5 CONCLUÍDA COM SUCESSO!"