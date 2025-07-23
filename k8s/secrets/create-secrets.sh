#!/bin/bash

# Script para criar Secrets do projeto lanchonete
# NUNCA commitar senhas reais no Git!

set -e

echo "🔐 Criando Secrets para o projeto lanchonete..."

# Secret para MySQL
echo "📊 Criando mysql-secret..."
kubectl create secret generic mysql-secret \
  --from-literal=MYSQL_ROOT_PASSWORD=root123 \
  --from-literal=MYSQL_USER=lanchonete \
  --from-literal=MYSQL_PASSWORD=lanchonete123 \
  --from-literal=DB_USERNAME=lanchonete \
  --from-literal=DB_PASSWORD=lanchonete123 \
  --dry-run=client -o yaml | kubectl apply -f -


echo "✅ Secrets criados com sucesso!"
echo
echo "🔍 Para verificar (NÃO mostra valores):"
echo "kubectl get secrets"
echo "kubectl describe secret mysql-secret"
echo
echo "⚠️  IMPORTANTE: Este script deve estar no .gitignore!"
echo "⚠️  NUNCA commitar senhas reais no repositório!"