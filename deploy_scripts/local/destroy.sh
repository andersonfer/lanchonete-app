#!/bin/bash

# Script para destruir a infraestrutura local (Minikube)

set -e

# Mudar para o diretório raiz do projeto
cd "$(dirname "$0")/../.."

echo "==================================================================="
echo "Destruindo infraestrutura local"
echo "==================================================================="

echo ""
echo "Deletando todos os recursos do Kubernetes..."
kubectl delete all --all
kubectl delete pvc --all
kubectl delete configmap --all
kubectl delete secret --all
kubectl delete hpa --all
kubectl delete ingress --all

echo ""
echo "Parando Minikube..."
minikube stop

echo ""
echo "Deletando cluster Minikube..."
minikube delete

echo ""
echo "==================================================================="
echo "Infraestrutura local destruida"
echo "==================================================================="
