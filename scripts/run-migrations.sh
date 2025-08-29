#!/bin/bash
set -e

echo "📋 EXECUTANDO MIGRATIONS SQL"
echo "============================"

# Obter endpoint do RDS
cd ../terraform/database
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
DB_NAME=$(terraform output -raw database_name)
cd ../../scripts

echo "🗄️ Conectando ao RDS: $RDS_ENDPOINT"

# Extrair host e porta
DB_HOST=$(echo $RDS_ENDPOINT | cut -d: -f1)
DB_PORT=$(echo $RDS_ENDPOINT | cut -d: -f2)

# Credenciais (usando as mesmas do tfvars)
DB_USERNAME="lanchonete"
DB_PASSWORD="LanchoneteDB123!"

echo "⏳ Aguardando RDS ficar disponível..."
while ! nc -z $DB_HOST $DB_PORT; do
    sleep 5
    echo "Aguardando conexão com $DB_HOST:$DB_PORT..."
done

echo "✅ RDS disponível!"

# Executar migrations via mysql client (se disponível)
if command -v mysql &> /dev/null; then
    echo "🔧 Executando migrations via mysql client..."
    
    # Schema
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p$DB_PASSWORD $DB_NAME < ../terraform/database/migrations/001_create_schema.sql
    echo "✅ Schema criado"
    
    # Seeds
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p$DB_PASSWORD $DB_NAME < ../terraform/database/migrations/002_seed_data.sql
    echo "✅ Dados inseridos"
    
else
    echo "❌ mysql client não encontrado"
    echo "💡 Instale: sudo apt-get install mysql-client"
    echo "💡 Ou execute manualmente as migrations em:"
    echo "   - ../terraform/database/migrations/001_create_schema.sql"
    echo "   - ../terraform/database/migrations/002_seed_data.sql"
    exit 1
fi

echo ""
echo "🎉 MIGRATIONS EXECUTADAS COM SUCESSO!"