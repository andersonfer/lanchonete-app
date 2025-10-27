#!/bin/bash
set -e

echo "🚀 Deploying to AWS EKS..."
echo ""

# Verifica contexto kubectl
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
echo "📌 Current kubectl context: $CURRENT_CONTEXT"
if [[ ! "$CURRENT_CONTEXT" =~ "eks" ]] && [[ ! "$CURRENT_CONTEXT" =~ "lanchonete" ]]; then
    echo "⚠️  Warning: Context doesn't look like EKS"
    echo "   Run: aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster"
    read -p "   Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 1. Build e push das imagens
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Step 1: Building and pushing images..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./deploy_scripts/aws/build-and-push.sh

# 2. Pega URLs ECR do Terraform
echo ""
echo "📋 Getting ECR URLs from Terraform..."
cd infra/ecr

ECR_CLIENTES=$(terraform output -json repositorios_ecr | jq -r '.clientes')
ECR_PEDIDOS=$(terraform output -json repositorios_ecr | jq -r '.pedidos')
ECR_COZINHA=$(terraform output -json repositorios_ecr | jq -r '.cozinha')
ECR_PAGAMENTO=$(terraform output -json repositorios_ecr | jq -r '.pagamento')

cd ../..

echo "   Clientes:  $ECR_CLIENTES:latest"
echo "   Pedidos:   $ECR_PEDIDOS:latest"
echo "   Cozinha:   $ECR_COZINHA:latest"
echo "   Pagamento: $ECR_PAGAMENTO:latest"

# 2. Creating secrets
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔐 Step 2: Creating secrets..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./deploy_scripts/aws/create-secrets.sh

# 3. Deploy databases (MongoDB e RabbitMQ em pods, MySQL via RDS)
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️  Step 3: Deploying pod databases..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   Note: MySQL via RDS (managed), only MongoDB and RabbitMQ in pods"
kubectl apply -f k8s/aws/statefulsets/

echo ""
echo "⏳ Waiting for pod databases to be ready..."
echo "   This may take 2-3 minutes..."

# Aguarda MongoDB
kubectl wait --for=condition=ready pod -l app=pagamento-mongodb --timeout=300s || true

# Aguarda RabbitMQ
kubectl wait --for=condition=ready pod -l app=shared-rabbitmq --timeout=300s || true

echo "✅ Pod databases ready!"

# 4. ConfigMaps
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚙️  Step 4: Applying ConfigMaps..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
kubectl apply -f k8s/base/configmaps/

# 5. Deploy microservices com substituição inline
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚢 Step 5: Deploying microservices..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "   Deploying clientes..."
sed "s|{{ECR_CLIENTES}}|${ECR_CLIENTES}:latest|g" \
    k8s/aws/deployments/clientes-deployment.yaml | kubectl apply -f -

echo "   Deploying pedidos..."
sed "s|{{ECR_PEDIDOS}}|${ECR_PEDIDOS}:latest|g" \
    k8s/aws/deployments/pedidos-deployment.yaml | kubectl apply -f -

echo "   Deploying cozinha..."
sed "s|{{ECR_COZINHA}}|${ECR_COZINHA}:latest|g" \
    k8s/aws/deployments/cozinha-deployment.yaml | kubectl apply -f -

echo "   Deploying pagamento..."
sed "s|{{ECR_PAGAMENTO}}|${ECR_PAGAMENTO}:latest|g" \
    k8s/aws/deployments/pagamento-deployment.yaml | kubectl apply -f -

echo "✅ Microservices deployed"

# 6. Services
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 Step 6: Deploying services..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
kubectl apply -f k8s/base/services/

echo ""
echo "⏳ Waiting for microservices to be ready..."
kubectl wait --for=condition=available deployment --all --timeout=300s || true

# 7. Ingress
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎊 Step 7: Applying Ingress (ALB)..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ -d "k8s/ingress/aws" ]; then
    kubectl apply -f k8s/ingress/aws/
    echo "⏳ ALB is provisioning (takes 3-5 minutes)..."
else
    echo "⚠️  No AWS ingress found"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Deploy complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Status
echo "📋 Current status:"
kubectl get pods
echo ""

# ALB URL
echo "🌐 Application Load Balancer:"
ALB_URL=$(kubectl get ingress -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
if [ -n "$ALB_URL" ]; then
    echo "   URL: http://$ALB_URL"
    echo ""
    echo "   Access URLs:"
    echo "   - http://$ALB_URL/clientes/actuator/health"
    echo "   - http://$ALB_URL/pedidos/actuator/health"
    echo "   - http://$ALB_URL/cozinha/actuator/health"
    echo "   - http://$ALB_URL/pagamento/actuator/health"
else
    echo "   ⏳ ALB not ready yet. Check with:"
    echo "      kubectl get ingress"
fi

echo ""
echo "💡 Useful commands:"
echo "   kubectl get pods              # Ver status dos pods"
echo "   kubectl logs <pod-name>       # Ver logs"
echo "   kubectl get ingress           # Ver URL do ALB"
echo ""
