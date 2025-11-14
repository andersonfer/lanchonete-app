#!/bin/bash
set -e

echo "🔨 Building and pushing images to ECR..."
echo ""

# Região AWS
AWS_REGION="us-east-1"

# Pega URLs dos repositórios ECR do Terraform
echo "📋 Getting ECR repository URLs from Terraform..."
cd infra/ecr

ECR_CLIENTES=$(terraform output -json repositorios_ecr | jq -r '.clientes')
ECR_PEDIDOS=$(terraform output -json repositorios_ecr | jq -r '.pedidos')
ECR_COZINHA=$(terraform output -json repositorios_ecr | jq -r '.cozinha')
ECR_PAGAMENTO=$(terraform output -json repositorios_ecr | jq -r '.pagamento')

cd ../..

echo "   Clientes:  $ECR_CLIENTES"
echo "   Pedidos:   $ECR_PEDIDOS"
echo "   Cozinha:   $ECR_COZINHA"
echo "   Pagamento: $ECR_PAGAMENTO"
echo ""

# Login no ECR
echo "🔐 Logging in to ECR..."
aws ecr get-login-password --region $AWS_REGION | \
    docker login --username AWS --password-stdin ${ECR_CLIENTES%%/*}

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Building and pushing: CLIENTES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd services/clientes
mvn clean package -DskipTests -q
docker build --network=host -t lanchonete-clientes:latest .
docker tag lanchonete-clientes:latest $ECR_CLIENTES:latest
docker push $ECR_CLIENTES:latest
cd ../..
echo "✅ Clientes pushed"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Building and pushing: PEDIDOS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd services/pedidos
mvn clean package -DskipTests -q
docker build --network=host -t lanchonete-pedidos:latest .
docker tag lanchonete-pedidos:latest $ECR_PEDIDOS:latest
docker push $ECR_PEDIDOS:latest
cd ../..
echo "✅ Pedidos pushed"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Building and pushing: COZINHA"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd services/cozinha
mvn clean package -DskipTests -q
docker build --network=host -t lanchonete-cozinha:latest .
docker tag lanchonete-cozinha:latest $ECR_COZINHA:latest
docker push $ECR_COZINHA:latest
cd ../..
echo "✅ Cozinha pushed"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Building and pushing: PAGAMENTO"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cd services/pagamento
mvn clean package -DskipTests -q
docker build --network=host -t lanchonete-pagamento:latest .
docker tag lanchonete-pagamento:latest $ECR_PAGAMENTO:latest
docker push $ECR_PAGAMENTO:latest
cd ../..
echo "✅ Pagamento pushed"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 All images built and pushed successfully!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Images in ECR:"
echo "   $ECR_CLIENTES:latest"
echo "   $ECR_PEDIDOS:latest"
echo "   $ECR_COZINHA:latest"
echo "   $ECR_PAGAMENTO:latest"
echo ""
