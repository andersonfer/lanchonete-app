#!/bin/bash

# Script seguro para criar Secrets do projeto lanchonete
# Usa variáveis de ambiente em vez de senhas hardcoded

set -e

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}🔐 Criando Secrets para o projeto lanchonete...${NC}"
echo ""

# Função para validar se variável existe e não está vazia
validate_env_var() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [ -z "$var_value" ]; then
        echo -e "${RED}❌ Erro: Variável $var_name não está definida!${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ $var_name definida${NC}"
    return 0
}

# Validar todas as variáveis necessárias
echo -e "${YELLOW}📋 Validando variáveis de ambiente...${NC}"

validate_env_var "MYSQL_ROOT_PASSWORD" || exit 1
validate_env_var "MYSQL_USER_PASSWORD" || exit 1

# Definir usuário padrão se não especificado
MYSQL_USER=${MYSQL_USER:-"lanchonete"}
DB_USERNAME=${DB_USERNAME:-"lanchonete"}
DB_PASSWORD=${DB_PASSWORD:-$MYSQL_USER_PASSWORD}

echo -e "${GREEN}✅ Todas as variáveis validadas!${NC}"
echo ""

# Mostrar configuração (sem senhas)
echo -e "${YELLOW}📊 Configuração:${NC}"
echo "   MYSQL_USER: $MYSQL_USER"
echo "   DB_USERNAME: $DB_USERNAME"
echo "   MYSQL_ROOT_PASSWORD: [OCULTA]"
echo "   MYSQL_USER_PASSWORD: [OCULTA]"
echo "   DB_PASSWORD: [OCULTA]"
echo ""

# Criar o Secret do MySQL
echo -e "${YELLOW}📊 Criando mysql-secret...${NC}"

kubectl create secret generic mysql-secret \
  --from-literal=MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
  --from-literal=MYSQL_USER="$MYSQL_USER" \
  --from-literal=MYSQL_PASSWORD="$MYSQL_USER_PASSWORD" \
  --from-literal=DB_USERNAME="$DB_USERNAME" \
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo -e "${GREEN}✅ Secrets criados com sucesso!${NC}"
echo ""
echo -e "${YELLOW}🔍 Para verificar (NÃO mostra valores):${NC}"
echo "kubectl get secrets"
echo "kubectl describe secret mysql-secret"
echo ""
echo -e "${GREEN}🛡️  SEGURANÇA: Senhas agora vêm de variáveis de ambiente!${NC}"
