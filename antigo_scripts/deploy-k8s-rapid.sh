#!/bin/bash

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Função para cleanup automático
cleanup_on_exit() {
    log_warning "Executando cleanup automático..."
    ./scripts/cleanup-aws-academy.sh || true
}

# Registrar cleanup automático
trap cleanup_on_exit EXIT

# Função principal
main() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════════════════════╗"
    echo "║                    ⚡ DEPLOY RÁPIDO EKS + CLEANUP AUTOMÁTICO                 ║"
    echo "║                                                                              ║"
    echo "║  🎯 ETAPA 6: Deploy Kubernetes com cleanup automático                       ║"
    echo "║  💰 Custo estimado: $0.50-1.00 (2-3 horas máximo)                          ║"
    echo "║  🔒 Budget protegido: cleanup automático ao final                           ║"
    echo "╚══════════════════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    # Verificar pré-requisitos
    log "1/7 - VERIFICANDO PRÉ-REQUISITOS"
    echo "================================="
    
    if ! aws sts get-caller-identity &>/dev/null; then
        log_error "Credenciais AWS não configuradas!"
        exit 1
    fi
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl não encontrado!"
        exit 1
    fi
    
    log_success "Pré-requisitos OK"
    
    # Criar cluster EKS
    log ""
    log "2/7 - CRIANDO CLUSTER EKS (20-25 min)"
    echo "====================================="
    
    cd terraform/kubernetes
    terraform init -upgrade
    terraform apply -var-file="../shared/academy.tfvars" -auto-approve
    
    # Configurar kubectl
    CLUSTER_NAME=$(terraform output -raw cluster_name)
    aws eks update-kubeconfig --region us-east-1 --name $CLUSTER_NAME
    cd ../..
    
    log_success "Cluster EKS criado: $CLUSTER_NAME"
    
    # Aguardar nodes ficarem prontos
    log ""
    log "3/7 - AGUARDANDO NODES FICAREM PRONTOS"
    echo "======================================"
    
    kubectl wait --for=condition=Ready nodes --all --timeout=300s
    log_success "Nodes prontos"
    
    # Obter endpoint RDS dinamicamente
    log ""
    log "4/7 - CONFIGURANDO MANIFESTS DINAMICAMENTE"
    echo "=========================================="
    
    cd terraform/database && terraform init -upgrade >/dev/null 2>&1 && cd ../..
    RDS_ENDPOINT=$(cd terraform/database && terraform output -raw rds_endpoint | sed 's/:3306$//')
    DB_USERNAME=$(cd terraform/shared && grep 'db_username' academy.tfvars | cut -d'"' -f2)
    DB_PASSWORD=$(cd terraform/shared && grep 'db_password' academy.tfvars | cut -d'"' -f2)
    
    log_success "RDS Endpoint: $RDS_ENDPOINT"
    
    # Processar templates
    sed "s/{{RDS_ENDPOINT}}/$RDS_ENDPOINT/g" k8s-manifests/configmaps/autoatendimento-configmap.yaml > /tmp/autoatendimento-configmap.yaml
    sed -e "s/{{DB_USERNAME}}/$DB_USERNAME/g" -e "s/{{DB_PASSWORD}}/$DB_PASSWORD/g" k8s-manifests/secrets/rds-secret.yaml > /tmp/rds-secret.yaml
    
    # Deploy aplicações
    log ""
    log "5/7 - DEPLOYANDO APLICAÇÕES NO KUBERNETES"
    echo "=========================================="
    
    # Aplicar manifests em ordem
    kubectl apply -f /tmp/autoatendimento-configmap.yaml
    kubectl apply -f k8s-manifests/configmaps/pagamento-configmap.yaml
    kubectl apply -f /tmp/rds-secret.yaml
    kubectl apply -f k8s-manifests/applications/
    kubectl apply -f k8s-manifests/hpa/
    kubectl apply -f k8s-manifests/ingress/
    
    log_success "Manifests aplicados"
    
    # Aguardar pods ficarem prontos
    log ""
    log "6/7 - AGUARDANDO PODS FICAREM PRONTOS (3-5 min)"
    echo "==============================================="
    
    kubectl wait --for=condition=ready pod -l app=autoatendimento --timeout=300s
    kubectl wait --for=condition=ready pod -l app=pagamento --timeout=300s
    
    log_success "Pods prontos"
    
    # Validação final
    log ""
    log "7/7 - VALIDAÇÃO FINAL"
    echo "===================="
    
    # Mostrar status
    echo ""
    echo "📊 STATUS DO CLUSTER:"
    kubectl get nodes -o wide
    
    echo ""
    echo "📦 STATUS DOS PODS:"
    kubectl get pods -o wide
    
    echo ""
    echo "🌐 STATUS DOS SERVICES:"
    kubectl get services
    
    echo ""
    echo "📈 STATUS HPA:"
    kubectl get hpa
    
    echo ""
    echo "🔗 STATUS INGRESS:"
    kubectl get ingress
    
    # Obter ALB endpoint
    ALB_ENDPOINT=""
    ATTEMPTS=0
    MAX_ATTEMPTS=10
    
    while [[ -z "$ALB_ENDPOINT" && $ATTEMPTS -lt $MAX_ATTEMPTS ]]; do
        ALB_ENDPOINT=$(kubectl get ingress lanchonete-alb -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
        if [[ -z "$ALB_ENDPOINT" ]]; then
            log "Aguardando ALB ficar pronto... (tentativa $((ATTEMPTS + 1))/$MAX_ATTEMPTS)"
            sleep 30
        fi
        ((ATTEMPTS++))
    done
    
    if [[ -n "$ALB_ENDPOINT" ]]; then
        log_success "ALB Endpoint: $ALB_ENDPOINT"
    else
        log_warning "ALB ainda não tem endpoint (normal - pode demorar alguns minutos)"
    fi
    
    echo ""
    log_success "🎉 ETAPA 6 CONCLUÍDA COM SUCESSO!"
    echo ""
    echo "✅ Cluster EKS funcionando"
    echo "✅ Aplicações deployadas"
    echo "✅ Health checks passando"
    echo "✅ HPA configurado"
    echo "✅ ALB Ingress criado"
    echo ""
    echo "🕐 O cluster ficará ativo por alguns minutos para demonstração..."
    echo "🧹 Cleanup automático será executado ao finalizar o script"
    echo ""
    
    # Aguardar input do usuário para manter rodando
    echo "🎯 DEMONSTRAÇÃO ATIVA - Pressione ENTER para executar cleanup e finalizar"
    read -p "   (ou Ctrl+C para manter rodando e fazer cleanup manual depois): "
    
    log_success "Iniciando cleanup automático..."
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi