#!/bin/bash
set -e

echo "🚀 Deploying to MINIKUBE..."
echo ""

# Verifica contexto
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
if [[ "$CURRENT_CONTEXT" != "minikube" ]]; then
    echo "⚠️  Warning: Not in Minikube context (current: $CURRENT_CONTEXT)"
    echo "   Continuing anyway..."
    echo ""
fi

# 1. Build das imagens
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 Step 1: Building images..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./deploy_scripts/local/build.sh

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔐 Step 2: Creating secrets..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./k8s/secrets/create-secrets.sh

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️  Step 3: Deploying databases..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
kubectl apply -f k8s/local/statefulsets/

echo ""
echo "⏳ Waiting for databases to be ready..."
echo "   This may take 2-3 minutes..."

# Aguarda MySQL instances
kubectl wait --for=condition=ready pod -l app=mysql-clientes --timeout=180s || true
kubectl wait --for=condition=ready pod -l app=mysql-pedidos --timeout=180s || true
kubectl wait --for=condition=ready pod -l app=mysql-cozinha --timeout=180s || true

# Aguarda MongoDB
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=180s || true

# Aguarda RabbitMQ
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=180s || true

echo "✅ Databases ready!"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚙️  Step 4: Applying ConfigMaps..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
kubectl apply -f k8s/base/configmaps/

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚢 Step 5: Deploying microservices..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
kubectl apply -f k8s/local/deployments/

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 Step 6: Deploying services..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
kubectl apply -f k8s/base/services/

echo ""
echo "⏳ Waiting for microservices to be ready..."
kubectl wait --for=condition=available deployment --all --timeout=300s || true

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎊 Step 7: Applying Ingress..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ -d "k8s/ingress/local" ]; then
    kubectl apply -f k8s/ingress/local/ || echo "⚠️  No ingress found or failed to apply"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Deploy complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Mostra status
echo "📋 Current status:"
kubectl get pods
echo ""

# Mostra URLs de acesso via Ingress
MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "unknown")
if [ "$MINIKUBE_IP" != "unknown" ]; then
    echo "🌐 Access URLs (via Ingress):"
    echo "   Minikube IP: $MINIKUBE_IP"
    echo ""
    echo "   Verifique o Ingress para as URLs corretas:"
    kubectl get ingress 2>/dev/null || echo "   (No ingress configured)"
else
    echo "⚠️  Could not determine Minikube IP"
fi

echo ""
echo "💡 Useful commands:"
echo "   kubectl get pods              # Ver status dos pods"
echo "   kubectl logs <pod-name>       # Ver logs"
echo "   kubectl get ingress           # Ver configuração do Ingress"
echo ""
