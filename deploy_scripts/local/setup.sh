#!/bin/bash

# Script para criar a infraestrutura local do zero (Minikube)

set -e

# Mudar para o diretório raiz do projeto
cd "$(dirname "$0")/../.."

echo "==================================================================="
echo "Criando infraestrutura local"
echo "==================================================================="

echo ""
echo "Iniciando Minikube..."
minikube start --memory=4096 --cpus=2

echo ""
echo "Configurando Docker para usar o daemon do Minikube..."
eval $(minikube docker-env)

echo ""
echo "Buildando imagens Docker..."
echo "Buildando lanchonete-clientes..."
(cd services/clientes && docker build -t lanchonete-clientes:latest .)

echo "Buildando lanchonete-pagamento..."
(cd services/pagamento && docker build -t lanchonete-pagamento:latest .)

echo "Buildando lanchonete-pedidos..."
(cd services/pedidos && docker build -t lanchonete-pedidos:latest .)

echo ""
echo "Criando secrets..."
bash k8s/secrets/create-secrets.sh

echo ""
echo "Aplicando manifests do Kubernetes..."
echo "Aplicando StatefulSets (Databases)..."
kubectl apply -f k8s/statefulsets/

echo ""
echo "Aguardando StatefulSets ficarem prontos..."
kubectl wait --for=condition=ready pod -l app=pagamento-mongodb --timeout=180s
kubectl wait --for=condition=ready pod -l app=clientes-mysql --timeout=180s
kubectl wait --for=condition=ready pod -l app=cozinha-mysql --timeout=180s
kubectl wait --for=condition=ready pod -l app=pedidos-mysql --timeout=180s
kubectl wait --for=condition=ready pod -l app=shared-rabbitmq --timeout=180s

echo ""
echo "Aplicando ConfigMaps..."
kubectl apply -f k8s/configmaps/

echo ""
echo "Aplicando Services..."
kubectl apply -f k8s/services/

echo ""
echo "Aplicando Ingress..."
kubectl apply -f k8s/ingress/local/

echo ""
echo "Aplicando Deployments..."
kubectl apply -f k8s/deployments/

echo ""
echo "Aplicando HPAs..."
kubectl apply -f k8s/hpa/

echo ""
echo "Aguardando Deployments ficarem prontos..."
kubectl wait --for=condition=available deployment/clientes-deployment --timeout=180s
kubectl wait --for=condition=available deployment/pagamento-deployment --timeout=180s
kubectl wait --for=condition=available deployment/pedidos-deployment --timeout=180s

echo ""
echo "==================================================================="
echo "Infraestrutura local criada com sucesso"
echo "==================================================================="

echo ""
echo "Status dos recursos:"
kubectl get pods
kubectl get svc
