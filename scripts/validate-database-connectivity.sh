#!/bin/bash
set -e

echo "🗄️ VALIDANDO CONECTIVIDADE COM RDS"
echo "=================================="

# Build Lambda de conectividade
echo "🔨 Compilando Lambda de conectividade..."
cd ../lambda-connectivity-test
mvn clean package -DskipTests
cd ../scripts

# Deploy infraestrutura completa
echo "🚀 Deployando infraestrutura database + Lambda..."
cd ../terraform/database
terraform apply -var-file="database.tfvars" -auto-approve
cd ../../scripts

# Aguardar Lambda ficar disponível
echo "⏳ Aguardando Lambda ficar disponível..."
sleep 30

# Testar conectividade via Lambda
echo "🧪 Testando conectividade via Lambda..."
LAMBDA_RESULT=$(aws lambda invoke \
    --function-name lanchonete-connectivity-test \
    --payload '{}' \
    --output text \
    response.json)

if [[ -f response.json ]]; then
    LAMBDA_OUTPUT=$(cat response.json)
    echo "📝 Resultado Lambda: $LAMBDA_OUTPUT"
    
    if [[ $LAMBDA_OUTPUT == *"success"* ]]; then
        echo "✅ CONECTIVIDADE VALIDADA COM SUCESSO!"
    else
        echo "❌ Falha na conectividade"
        echo "📋 Verifique logs da Lambda no CloudWatch"
        exit 1
    fi
    rm -f response.json
else
    echo "❌ Erro ao executar Lambda"
    exit 1
fi

echo ""
echo "🎉 ETAPA 1 COMPLETA!"
echo "   - RDS MySQL rodando"
echo "   - Lambda conectando com sucesso"
echo "   - Security Groups configurados"