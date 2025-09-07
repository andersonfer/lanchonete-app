#!/bin/bash

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║              🚀 DEPLOY HÍBRIDO - ETAPA 6                      ║"
    echo "║                                                               ║"
    echo "║  Terraform: ECR + Docker Build (Nativo)                      ║"
    echo "║  Kubernetes: YAML Templates (Flexível)                       ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    # 1. Terraform - ECR + Docker Build
    log_info "1/4 - TERRAFORM: ECR + DOCKER BUILD"
    echo "==================================="
    
    cd terraform/kubernetes
    
    log_info "Inicializando Terraform..."
    terraform init -upgrade
    
    log_info "Aplicando recursos (ECR + Build + Push)..."
    terraform apply -var-file="../shared/academy.tfvars" -auto-approve
    
    # Obter outputs do Terraform
    ECR_URI_AUTOATENDIMENTO=$(terraform output -raw ecr_autoatendimento_url)
    ECR_URI_PAGAMENTO=$(terraform output -raw ecr_pagamento_url)
    CLUSTER_NAME=$(terraform output -raw cluster_name)
    
    cd ../..
    
    log_success "ECR + Docker Build concluído"
    log_success "Autoatendimento: $ECR_URI_AUTOATENDIMENTO"
    log_success "Pagamento: $ECR_URI_PAGAMENTO"
    
    # 2. Configurar kubectl
    log_info ""
    log_info "2/4 - CONFIGURANDO KUBECTL"
    echo "=========================="
    
    aws eks update-kubeconfig --region us-east-1 --name $CLUSTER_NAME
    kubectl wait --for=condition=Ready nodes --all --timeout=600s
    
    log_success "kubectl configurado e nodes prontos"
    
    # 3. Processar Templates
    log_info ""
    log_info "3/4 - PROCESSANDO TEMPLATES YAML"
    echo "================================"
    
    # Obter configurações dinâmicas
    cd terraform/database && terraform init -upgrade >/dev/null 2>&1 && cd ../..
    RDS_ENDPOINT=$(cd terraform/database && terraform output -raw rds_endpoint | sed 's/:3306$//')
    
    log_success "RDS Endpoint: $RDS_ENDPOINT"
    
    # Processar templates ConfigMaps
    sed "s/{{RDS_ENDPOINT}}/$RDS_ENDPOINT/g" k8s-manifests/configmaps/autoatendimento-configmap.yaml > /tmp/autoatendimento-configmap.yaml
    
    # Processar templates Deployments
    sed "s|{{ECR_URI_AUTOATENDIMENTO}}|$ECR_URI_AUTOATENDIMENTO:latest|g" k8s-manifests/applications/autoatendimento-deployment.yaml > /tmp/autoatendimento-deployment.yaml
    sed "s|{{ECR_URI_PAGAMENTO}}|$ECR_URI_PAGAMENTO:latest|g" k8s-manifests/applications/pagamento-deployment.yaml > /tmp/pagamento-deployment.yaml
    
    log_success "Templates processados"
    
    # 4. Deploy Kubernetes
    log_info ""
    log_info "4/4 - DEPLOY KUBERNETES"
    echo "======================="
    
    # Aplicar manifests em ordem
    log_info "Aplicando ConfigMaps..."
    kubectl apply -f /tmp/autoatendimento-configmap.yaml
    kubectl apply -f k8s-manifests/configmaps/pagamento-configmap.yaml
    
    log_info "Aplicando Secrets..."
    kubectl delete secret rds-secret 2>/dev/null || true
    kubectl create secret generic rds-secret --from-literal=DB_USERNAME=lanchonete --from-literal=DB_PASSWORD=LanchoneteDB123!
    
    log_info "Aplicando Deployments..."
    kubectl apply -f /tmp/autoatendimento-deployment.yaml
    kubectl apply -f /tmp/pagamento-deployment.yaml
    
    log_info "Aplicando Services..."
    kubectl apply -f k8s-manifests/applications/services.yaml
    
    log_info "Aplicando HPA..."
    kubectl apply -f k8s-manifests/hpa/
    
    log_info "Pulando Ingress (usando NLB diretamente)..."
    # kubectl apply -f k8s-manifests/ingress/  # Não temos ALB Ingress Controller
    
    log_success "Todos os manifests aplicados"
    
    # 5. Aguardar pods ficarem prontos
    log_info ""
    log_info "5/4 - AGUARDANDO PODS FICAREM PRONTOS"
    echo "===================================="
    
    log_info "Aguardando autoatendimento pods..."
    kubectl wait --for=condition=ready pod -l app=autoatendimento --timeout=300s
    
    log_info "Aguardando pagamento pods..."
    kubectl wait --for=condition=ready pod -l app=pagamento --timeout=300s
    
    # 6. Validação final
    echo ""
    log_success "🎉 DEPLOY HÍBRIDO CONCLUÍDO!"
    echo ""
    echo "📊 STATUS FINAL:"
    echo "================"
    
    echo ""
    echo "🖥️  NODES:"
    kubectl get nodes -o wide
    
    echo ""
    echo "📦 PODS:"
    kubectl get pods -o wide
    
    echo ""
    echo "🌐 SERVICES:"
    kubectl get services
    
    echo ""
    echo "📈 HPA:"
    kubectl get hpa
    
    echo ""
    echo "🔗 LOAD BALANCER:"
    aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[*].[LoadBalancerName,Type,Scheme,DNSName]' --output table
    
    # Mostrar endpoint NLB disponível
    NLB_ENDPOINT=$(aws elbv2 describe-load-balancers --names lanchonete-nlb --query 'LoadBalancers[0].DNSName' --output text 2>/dev/null || echo "")
    if [[ -n "$NLB_ENDPOINT" && "$NLB_ENDPOINT" != "None" ]]; then
        log_success "📡 NLB Endpoint: $NLB_ENDPOINT"
        log_info "💡 NLB será usado pelo API Gateway via VPC Link"
    else
        log_warning "📡 NLB não encontrado"
    fi
    
    echo ""
    log_success "✅ ETAPA 6 - DEPLOY KUBERNETES CONCLUÍDO!"
    echo ""
    echo "🔗 Próximo passo: ETAPA 7 - Integração API Gateway ↔ EKS"
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi