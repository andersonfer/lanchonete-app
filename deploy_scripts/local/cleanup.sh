#!/bin/bash
set -e

echo "🧹 Cleaning up Minikube resources..."
echo ""

# Verifica contexto
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
if [[ "$CURRENT_CONTEXT" != "minikube" ]]; then
    echo "⚠️  Warning: Not in Minikube context (current: $CURRENT_CONTEXT)"
    echo "   Continuing anyway..."
    echo ""
fi

echo "Deleting Ingress..."
kubectl delete -f k8s/ingress/local/ --ignore-not-found=true

echo ""
echo "Deleting microservices..."
kubectl delete -f k8s/local/deployments/ --ignore-not-found=true

echo ""
echo "Deleting services..."
kubectl delete -f k8s/base/services/ --ignore-not-found=true

echo ""
echo "Deleting databases..."
kubectl delete -f k8s/local/statefulsets/ --ignore-not-found=true

echo ""
echo "Deleting ConfigMaps..."
kubectl delete -f k8s/base/configmaps/ --ignore-not-found=true

echo ""
echo "Deleting Secrets..."
kubectl delete secret --all --ignore-not-found=true

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "📋 Remaining resources:"
kubectl get all
