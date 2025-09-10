#!/bin/bash

# Script para aplicar manifests Kubernetes no cluster
# Uso: ./scripts/deploy-k8s.sh

set -e

echo "🚀 Iniciando deploy no Kubernetes..."

echo "🔍 Verificando conexão com cluster..."
kubectl cluster-info --context $(kubectl config current-context) | head -1

echo "📦 Aplicando ConfigMaps..."
kubectl apply -f k8s_manifests/autoatendimento/autoatendimento-configmap.yaml
kubectl apply -f k8s_manifests/pagamento/pagamento-configmap.yaml

echo "🚀 Aplicando Deployments..."
kubectl apply -f k8s_manifests/autoatendimento/autoatendimento-deployment.yaml
kubectl apply -f k8s_manifests/pagamento/pagamento-deployment.yaml

echo "🌐 Aplicando Services..."
kubectl apply -f k8s_manifests/autoatendimento/autoatendimento-service.yml
kubectl apply -f k8s_manifests/pagamento/pagamento-service.yaml

echo "📈 Aplicando HPAs..."
kubectl apply -f k8s_manifests/autoatendimento/autoatendimento-hpa.yml
kubectl apply -f k8s_manifests/pagamento/pagamento-hpa.yml

echo "⏳ Aguardando pods ficarem prontos..."
echo "  Aguardando autoatendimento..."
kubectl wait --for=condition=available --timeout=300s deployment/autoatendimento-deployment

echo "  Aguardando pagamento..."
kubectl wait --for=condition=available --timeout=300s deployment/pagamento-deployment

echo "✅ Deploy completo!"

echo "📋 Status dos recursos:"
echo ""
echo "🏗️  DEPLOYMENTS:"
kubectl get deployments -o wide

echo ""
echo "🌐 SERVICES:"
kubectl get services -o wide

echo ""
echo "📦 PODS:"
kubectl get pods -o wide

echo ""
echo "📈 HPAs:"
kubectl get hpa

echo ""
echo "🎯 Para acessar as aplicações:"
echo "  Autoatendimento: http://$(kubectl get service autoatendimento-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'):8080"
echo "  Pagamento: http://$(kubectl get service pagamento-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'):8081"

echo "📋 Deploy finalizado com sucesso!"