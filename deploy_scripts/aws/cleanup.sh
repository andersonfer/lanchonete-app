#!/bin/bash
set -e

echo "🧹 Cleaning up AWS EKS resources..."
echo ""

# Verifica contexto
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
if [[ ! "$CURRENT_CONTEXT" =~ "eks" ]] && [[ ! "$CURRENT_CONTEXT" =~ "lanchonete" ]]; then
    echo "⚠️  Warning: Context doesn't look like EKS (current: $CURRENT_CONTEXT)"
    echo "   Continuing anyway..."
    echo ""
fi

echo "Deleting Ingress..."
kubectl delete -f k8s/ingress/aws/ --ignore-not-found=true

echo ""
echo "Deleting microservices..."
kubectl delete deployment --all --ignore-not-found=true

echo ""
echo "Deleting services..."
kubectl delete -f k8s/base/services/ --ignore-not-found=true

echo ""
echo "Deleting databases..."
kubectl delete -f k8s/aws/statefulsets/ --ignore-not-found=true

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

echo ""
echo "💡 To destroy infrastructure:"
echo "   cd infra/kubernetes && terraform destroy"
echo "   cd infra/ecr && terraform destroy"
echo ""
