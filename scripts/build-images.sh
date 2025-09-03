#!/bin/bash

set -e

echo "🐳 BUILDING DOCKER IMAGES"
echo "========================"

# Verificar se Docker está rodando
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker não está rodando"
    exit 1
fi

# Build autoatendimento
echo "🍔 1/2 - Building autoatendimento image..."
cd applications/autoatendimento
docker build -t lanchonete/autoatendimento:latest .
echo "✅ autoatendimento image built successfully"
cd ../..

# Build pagamento
echo "💳 2/2 - Building pagamento image..."
cd applications/pagamento
docker build -t lanchonete/pagamento:latest .
echo "✅ pagamento image built successfully"
cd ../..

echo ""
echo "✅ TODAS AS IMAGENS BUILDADAS COM SUCESSO!"
echo "========================================="

# Listar imagens criadas
echo "📦 Imagens criadas:"
docker images | grep lanchonete

echo ""
echo "🚀 PRÓXIMO PASSO: Execute ./scripts/push-to-ecr.sh (se necessário)"