#!/bin/bash
# =============================================================================
# DEPLOY DATABASE - PROJETO LANCHONETE
# =============================================================================

set -e

echo "🚀 DEPLOY DATABASE RDS"
echo "====================="

# Verificar se está no diretório correto
if [ ! -f "terraform/database/main.tf" ]; then
    echo "❌ Execute este script na raiz do projeto"
    exit 1
fi

# Deploy Terraform
echo "🏗️  1/4 - DEPLOYING RDS..."
cd terraform/database

terraform init -upgrade
echo "📋 Executando terraform plan..."
terraform plan -var-file="../shared/academy.tfvars"

echo "🚀 Executando terraform apply..."
terraform apply -var-file="../shared/academy.tfvars" -auto-approve

# Obter informações do RDS
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
DB_NAME=$(terraform output -raw database_name)

echo "✅ RDS criado: $RDS_ENDPOINT"

cd ../..

# Aguardar RDS ficar disponível
echo "⏳ 2/4 - AGUARDANDO RDS FICAR DISPONÍVEL..."
echo "💡 Isso pode demorar 10-15 minutos..."

# Aguardar porta ficar acessível
for i in {1..30}; do
    if timeout 5 bash -c "cat </dev/null > /dev/tcp/${RDS_ENDPOINT%:*}/3306" 2>/dev/null; then
        echo "✅ RDS está acessível!"
        break
    fi
    echo "⏳ Tentativa $i/30 - aguardando..."
    sleep 30
done

# Executar migrations
echo "📊 3/4 - EXECUTANDO MIGRATIONS..."
if command -v mysql &> /dev/null; then
    DB_USER="lanchonete_admin"
    DB_PASS="LanchoneteDB123!"
    
    echo "📋 Criando schema..."
    mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" < terraform/database/migrations/001_create_schema.sql
    
    echo "📋 Inserindo dados iniciais..."
    mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" < terraform/database/migrations/002_seed_data.sql
    
    echo "✅ Migrations executadas com sucesso!"
else
    echo "⚠️  MySQL client não encontrado. Instale com: sudo apt install mysql-client"
    echo "💡 Execute as migrations manualmente depois"
fi

# Validação final
echo "🔍 4/4 - EXECUTANDO VALIDAÇÃO..."
./scripts/validate-database.sh

echo ""
echo "✅ DEPLOY DATABASE CONCLUÍDO COM SUCESSO!"
echo "======================================"
echo "📡 RDS Endpoint: $RDS_ENDPOINT"
echo "🗄️  Database: $DB_NAME"
echo "🔑 Credenciais: lanchonete_admin / LanchoneteDB123!"
echo ""
echo "💡 PRÓXIMOS PASSOS:"
echo "   - Marque como concluída: 'ETAPA 1 concluída'"
echo "   - Continue para ETAPA 2: Lambda de Autenticação"