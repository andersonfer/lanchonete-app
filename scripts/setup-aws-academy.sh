#!/bin/bash
# =============================================================================
# SETUP AWS ACADEMY - PROJETO LANCHONETE
# =============================================================================

set -e

echo "🚀 CONFIGURANDO AMBIENTE AWS ACADEMY"
echo "===================================="

# Verificar AWS CLI
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI não encontrado. Instale primeiro."
    exit 1
fi

# Verificar credenciais
echo "🔍 Verificando credenciais AWS..."
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
    echo "⚠️  Warning: Não está usando LabRole - pode haver limitações"
fi

# Configurar região padrão
export AWS_DEFAULT_REGION=us-east-1
echo "✅ Região configurada: $AWS_DEFAULT_REGION"

# Verificar Terraform
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform não encontrado."
    echo "💡 Instale com: sudo apt install terraform"
    exit 1
fi

TERRAFORM_VERSION=$(terraform version -json | jq -r '.terraform_version')
echo "✅ Terraform versão: $TERRAFORM_VERSION"

echo ""
echo "✅ AMBIENTE CONFIGURADO COM SUCESSO!"
echo "💡 Para deploy do database:"
echo "   ./scripts/deploy-database.sh"
echo ""
echo "⚠️  LEMBRE-SE: Credenciais AWS Academy expiram em 4 horas!"