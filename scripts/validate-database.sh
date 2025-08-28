#!/bin/bash
# =============================================================================
# VALIDAÇÃO DO DATABASE - PROJETO LANCHONETE
# =============================================================================

set -e

echo "🔍 VALIDANDO DATABASE RDS"
echo "========================="

# Obter outputs do Terraform
cd terraform/database

if [ ! -f terraform.tfstate ]; then
    echo "❌ Terraform state não encontrado. Execute terraform apply primeiro."
    exit 1
fi

RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
DB_NAME=$(terraform output -raw database_name)

echo "📡 RDS Endpoint: $RDS_ENDPOINT"
echo "🗄️  Database: $DB_NAME"

# Validar se RDS está acessível
echo "🔍 Testando conectividade..."
if timeout 10 bash -c "cat </dev/null > /dev/tcp/${RDS_ENDPOINT%:*}/3306"; then
    echo "✅ RDS está acessível na porta 3306"
else
    echo "❌ RDS não está acessível. Verifique:"
    echo "   1. Security Groups"
    echo "   2. Se RDS terminou de criar"
    echo "   3. Se está na VPC correta"
    exit 1
fi

# Tentar conectar com MySQL (se disponível)
if command -v mysql &> /dev/null; then
    echo "🔍 Testando conexão MySQL..."
    
    # Usar credenciais do .tfvars
    DB_USER="lanchonete_admin"
    DB_PASS="LanchoneteDB123!"
    
    if mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e "SELECT 1;" > /dev/null 2>&1; then
        echo "✅ Conexão MySQL funcionando"
        
        # Testar se tabelas existem
        TABLES=$(mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e "SHOW TABLES;" 2>/dev/null | wc -l)
        if [ $TABLES -gt 4 ]; then
            echo "✅ Tabelas criadas: $((TABLES-1))"
        else
            echo "⚠️  Poucas tabelas encontradas. Execute as migrations."
        fi
        
        # Testar se dados existem
        PRODUTOS=$(mysql -h "$RDS_ENDPOINT" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e "SELECT COUNT(*) FROM produto;" 2>/dev/null | tail -n1)
        if [ "$PRODUTOS" -gt 0 ]; then
            echo "✅ Produtos inseridos: $PRODUTOS"
        else
            echo "⚠️  Nenhum produto encontrado. Execute seed data."
        fi
        
    else
        echo "❌ Falha na conexão MySQL. Verifique credenciais."
    fi
else
    echo "⚠️  MySQL client não instalado. Instale com: sudo apt install mysql-client"
fi

echo ""
echo "✅ VALIDAÇÃO CONCLUÍDA!"
echo "💡 Se houver problemas, verifique os logs do CloudWatch"