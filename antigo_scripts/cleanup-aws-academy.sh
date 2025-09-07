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

# Verificar credenciais AWS
check_aws_credentials() {
    if ! aws sts get-caller-identity &>/dev/null; then
        log_error "Credenciais AWS não configuradas ou expiradas!"
        log_warning "Execute os comandos de export do AWS Academy primeiro"
        exit 1
    fi
    log_success "Credenciais AWS verificadas"
}

# Listar recursos caros
list_expensive_resources() {
    log "🔍 Verificando recursos caros..."
    echo ""
    
    # EKS Clusters
    local eks_clusters=$(aws eks list-clusters --query 'clusters' --output text 2>/dev/null || echo "")
    if [[ -n "$eks_clusters" ]]; then
        echo "☸️  EKS CLUSTERS (mais caro: ~$2.40/dia por cluster):"
        for cluster in $eks_clusters; do
            local status=$(aws eks describe-cluster --name $cluster --query 'cluster.status' --output text 2>/dev/null)
            echo "   - $cluster ($status)"
        done
        echo ""
    fi
    
    # Load Balancers
    local load_balancers=$(aws elbv2 describe-load-balancers --query "LoadBalancers[?State.Code=='active'].[LoadBalancerName,Type]" --output text 2>/dev/null || echo "")
    if [[ -n "$load_balancers" ]]; then
        echo "📡 LOAD BALANCERS (~$0.54/dia por LB):"
        echo "$load_balancers" | while read lb_name lb_type; do
            if [[ -n "$lb_name" ]]; then
                echo "   - $lb_name ($lb_type)"
            fi
        done
        echo ""
    fi
    
    # RDS Instances
    local rds_instances=$(aws rds describe-db-instances --query "DBInstances[?DBInstanceStatus=='available'].[DBInstanceIdentifier,DBInstanceClass]" --output text 2>/dev/null || echo "")
    if [[ -n "$rds_instances" ]]; then
        echo "🗄️  RDS INSTANCES (~$0.41/dia por instância):"
        echo "$rds_instances" | while read db_id db_class; do
            if [[ -n "$db_id" ]]; then
                echo "   - $db_id ($db_class)"
            fi
        done
        echo ""
    fi
}

# Confirmar ação
confirm_action() {
    local message="$1"
    echo -e "${YELLOW}$message${NC}"
    read -p "Confirmar? (s/N): " -n 1 -r
    echo ""
    [[ $REPLY =~ ^[Ss]$ ]]
}

# Limpar EKS clusters
cleanup_eks() {
    local eks_clusters=$(aws eks list-clusters --query 'clusters' --output text 2>/dev/null || echo "")
    
    if [[ -z "$eks_clusters" ]]; then
        log_success "Nenhum cluster EKS encontrado"
        return 0
    fi
    
    for cluster in $eks_clusters; do
        log "Deletando EKS cluster: $cluster"
        
        # Deletar node groups primeiro
        local node_groups=$(aws eks list-nodegroups --cluster-name $cluster --query 'nodegroups' --output text 2>/dev/null || echo "")
        for nodegroup in $node_groups; do
            log "  Deletando node group: $nodegroup"
            aws eks delete-nodegroup --cluster-name $cluster --nodegroup-name $nodegroup >/dev/null 2>&1 || true
        done
        
        # Aguardar node groups serem deletados
        if [[ -n "$node_groups" ]]; then
            log "  Aguardando node groups serem deletados..."
            for nodegroup in $node_groups; do
                aws eks wait nodegroup-deleted --cluster-name $cluster --nodegroup-name $nodegroup 2>/dev/null || true
            done
        fi
        
        # Deletar cluster
        log "  Deletando cluster: $cluster"
        aws eks delete-cluster --name $cluster >/dev/null 2>&1
        
        log_success "Cluster EKS $cluster em processo de deleção"
    done
}

# Limpar Load Balancers
cleanup_load_balancers() {
    local load_balancers=$(aws elbv2 describe-load-balancers --query "LoadBalancers[?State.Code=='active'].[LoadBalancerArn,LoadBalancerName]" --output text 2>/dev/null || echo "")
    
    if [[ -z "$load_balancers" ]]; then
        log_success "Nenhum Load Balancer encontrado"
        return 0
    fi
    
    echo "$load_balancers" | while read lb_arn lb_name; do
        if [[ -n "$lb_arn" ]]; then
            log "Deletando Load Balancer: $lb_name"
            aws elbv2 delete-load-balancer --load-balancer-arn "$lb_arn" >/dev/null 2>&1
            log_success "Load Balancer $lb_name deletado"
        fi
    done
}

# Limpar RDS
cleanup_rds() {
    local rds_instances=$(aws rds describe-db-instances --query "DBInstances[?DBInstanceStatus=='available'].DBInstanceIdentifier" --output text 2>/dev/null || echo "")
    
    if [[ -z "$rds_instances" ]]; then
        log_success "Nenhuma instância RDS encontrada"
        return 0
    fi
    
    for db_instance in $rds_instances; do
        log "Parando instância RDS: $db_instance (pode ser religada depois)"
        aws rds stop-db-instance --db-instance-identifier $db_instance >/dev/null 2>&1 || true
        log_success "RDS $db_instance parado"
    done
}

# Função principal
main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║              🧹 CLEANUP AWS ACADEMY - RECURSOS CAROS          ║"
    echo "║                                                               ║"
    echo "║  Remove apenas recursos que geram custos altos:               ║"
    echo "║  • EKS Clusters (~$2.40/dia)                                  ║"
    echo "║  • Load Balancers (~$0.54/dia)                                ║"
    echo "║  • RDS Instances (~$0.41/dia) - apenas PARA                   ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    check_aws_credentials
    list_expensive_resources
    
    if confirm_action "🔴 LIMPAR TODOS OS RECURSOS CAROS listados acima?"; then
        echo ""
        log "Iniciando limpeza dos recursos caros..."
        
        cleanup_eks
        cleanup_load_balancers  
        cleanup_rds
        
        echo ""
        log_success "Limpeza finalizada!"
        log_warning "RDS foi PARADO (não deletado) - pode ser religado depois"
        log_warning "EKS e Load Balancers foram DELETADOS permanentemente"
    else
        log_warning "Nenhuma alteração feita"
    fi
    
    echo ""
    log_warning "LEMBRE-SE: Execute este script ao final de cada sessão AWS Academy"
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi