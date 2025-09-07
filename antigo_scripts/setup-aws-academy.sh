#!/bin/bash
set -e

echo "🎓 CONFIGURANDO AMBIENTE AWS ACADEMY"
echo "===================================="

# Verificar AWS CLI
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI não encontrado. Instale primeiro."
    exit 1
fi

# Verificar credenciais
echo "🔍 Verificando credenciais AWS Academy..."
CALLER_IDENTITY=$(aws sts get-caller-identity 2>/dev/null || echo "error")

if [[ $CALLER_IDENTITY == "error" ]]; then
    echo "❌ Credenciais AWS não configuradas!"
    echo "💡 No AWS Academy:"
    echo "   1. Acesse 'AWS Details'"
    echo "   2. Clique em 'AWS CLI'"
    echo "   3. Copie e execute os comandos export"
    exit 1
fi

# Verificar se é LabRole
ROLE_ARN=$(echo $CALLER_IDENTITY | jq -r '.Arn')
if [[ $ROLE_ARN == *"LabRole"* ]]; then
    echo "✅ LabRole detectada: OK"
else
    echo "⚠️  Warning: Não está usando LabRole"
fi

# Configurar região
export AWS_DEFAULT_REGION=us-east-1
echo "✅ Região configurada: $AWS_DEFAULT_REGION"

# Verificar Terraform
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform não encontrado."
    exit 1
fi

TERRAFORM_VERSION=$(terraform version -json | jq -r '.terraform_version')
echo "✅ Terraform versão: $TERRAFORM_VERSION"

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não encontrado."
    exit 1
fi

echo ""
echo "✅ AMBIENTE CONFIGURADO COM SUCESSO!"
echo "💡 Próximos passos:"
echo "   1. cd terraform/database && terraform init && terraform apply -var-file=\"../shared/academy.tfvars\""
echo "   2. ./scripts/validate-database-connectivity.sh"
echo ""
echo "⚠️  LEMBRE-SE: Credenciais AWS Academy expiram em 4 horas!"