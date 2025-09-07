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

# Verificar credenciais AWS
check_aws_credentials() {
    if ! aws sts get-caller-identity &>/dev/null; then
        log_error "Credenciais AWS não configuradas ou expiradas!"
        echo "Execute os comandos de export do AWS Academy primeiro"
        exit 1
    fi
}

# Verificar EKS clusters
check_eks() {
    echo "☸️  VERIFICANDO EKS CLUSTERS..."
    
    local eks_clusters=$(aws eks list-clusters --query 'clusters' --output text 2>/dev/null || echo "")
    
    if [[ -z "$eks_clusters" ]]; then
        log_success "Nenhum cluster EKS ativo (economia: \$2.40+/dia)"
        return 0
    fi
    
    local has_active=false
    for cluster in $eks_clusters; do
        local status=$(aws eks describe-cluster --name $cluster --query 'cluster.status' --output text 2>/dev/null)
        case $status in
            "ACTIVE")
                log_error "Cluster EKS ATIVO: $cluster (custo: ~\$2.40/dia)"
                has_active=true
                ;;
            "DELETING")
                log_warning "Cluster EKS sendo deletado: $cluster"
                ;;
            *)
                log_info "Cluster EKS: $cluster ($status)"
                ;;
        esac
    done
    
    if [[ "$has_active" == "false" ]]; then
        log_success "Nenhum cluster EKS ativo"
    fi
}

# Verificar instâncias EC2
check_ec2() {
    echo ""
    echo "🖥️  VERIFICANDO INSTÂNCIAS EC2..."
    
    local running_instances=$(aws ec2 describe-instances --query "Reservations[*].Instances[?State.Name=='running'].[InstanceId,InstanceType,Tags[?Key=='Name'].Value|[0]]" --output text 2>/dev/null || echo "")
    
    if [[ -z "$running_instances" ]]; then
        log_success "Nenhuma instância EC2 rodando (economia variável)"
        return 0
    fi
    
    local has_running=false
    echo "$running_instances" | while read instance_id instance_type instance_name; do
        if [[ -n "$instance_id" ]]; then
            if [[ -z "$instance_name" ]]; then
                instance_name="(sem nome)"
            fi
            log_error "Instância EC2 RODANDO: $instance_id ($instance_type) - $instance_name"
            has_running=true
        fi
    done
    
    if [[ "$has_running" == "false" ]]; then
        log_success "Nenhuma instância EC2 rodando"
    fi
}

# Verificar RDS
check_rds() {
    echo ""
    echo "🗄️  VERIFICANDO INSTÂNCIAS RDS..."
    
    local rds_instances=$(aws rds describe-db-instances --query "DBInstances[?DBInstanceStatus!='deleting'].[DBInstanceIdentifier,DBInstanceStatus,DBInstanceClass]" --output text 2>/dev/null || echo "")
    
    if [[ -z "$rds_instances" ]]; then
        log_success "Nenhuma instância RDS ativa (economia: \$0.41+/dia)"
        return 0
    fi
    
    local has_available=false
    echo "$rds_instances" | while read db_id status db_class; do
        if [[ -n "$db_id" ]]; then
            case $status in
                "available")
                    log_error "RDS ATIVO: $db_id ($db_class) - custo: ~\$0.41/dia"
                    has_available=true
                    ;;
                "stopped")
                    log_success "RDS PARADO: $db_id ($db_class) - sem custo"
                    ;;
                "stopping")
                    log_warning "RDS sendo parado: $db_id ($db_class)"
                    ;;
                *)
                    log_info "RDS: $db_id ($status, $db_class)"
                    ;;
            esac
        fi
    done
    
    if [[ "$has_available" == "false" ]]; then
        log_success "Nenhuma instância RDS cobrando"
    fi
}

# Verificar Load Balancers
check_load_balancers() {
    echo ""
    echo "📡 VERIFICANDO LOAD BALANCERS..."
    
    local load_balancers=$(aws elbv2 describe-load-balancers --query "LoadBalancers[?State.Code=='active'].[LoadBalancerName,Type,State.Code]" --output text 2>/dev/null || echo "")
    
    if [[ -z "$load_balancers" ]]; then
        log_success "Nenhum Load Balancer ativo (economia: \$0.54+/dia)"
        return 0
    fi
    
    local has_active=false
    echo "$load_balancers" | while read lb_name lb_type state; do
        if [[ -n "$lb_name" ]]; then
            log_error "Load Balancer ATIVO: $lb_name ($lb_type) - custo: ~\$0.54/dia"
            has_active=true
        fi
    done
    
    if [[ "$has_active" == "false" ]]; then
        log_success "Nenhum Load Balancer ativo"
    fi
}

# Verificar NAT Gateways
check_nat_gateways() {
    echo ""
    echo "🌐 VERIFICANDO NAT GATEWAYS..."
    
    local nat_gateways=$(aws ec2 describe-nat-gateways --query "NatGateways[?State=='available'].[NatGatewayId,State]" --output text 2>/dev/null || echo "")
    
    if [[ -z "$nat_gateways" ]]; then
        log_success "Nenhum NAT Gateway ativo (economia: \$1.08+/dia)"
        return 0
    fi
    
    echo "$nat_gateways" | while read nat_id state; do
        if [[ -n "$nat_id" ]]; then
            log_error "NAT Gateway ATIVO: $nat_id - custo: ~\$1.08/dia"
        fi
    done
}

# Resumo final
show_summary() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "${BLUE}📊 RESUMO DA VERIFICAÇÃO${NC}"
    echo "═══════════════════════════════════════════════════════════════"
    
    # Contar recursos ativos
    local active_eks=$(aws eks list-clusters --query 'length(clusters)' --output text 2>/dev/null || echo "0")
    local running_ec2=$(aws ec2 describe-instances --query "length(Reservations[*].Instances[?State.Name=='running'])" --output text 2>/dev/null || echo "0")
    local available_rds=$(aws rds describe-db-instances --query "length(DBInstances[?DBInstanceStatus=='available'])" --output text 2>/dev/null || echo "0")
    local active_lbs=$(aws elbv2 describe-load-balancers --query "length(LoadBalancers[?State.Code=='active'])" --output text 2>/dev/null || echo "0")
    local active_nats=$(aws ec2 describe-nat-gateways --query "length(NatGateways[?State=='available'])" --output text 2>/dev/null || echo "0")
    
    local total_expensive=$((active_eks + running_ec2 + available_rds + active_lbs + active_nats))
    
    if [[ $total_expensive -eq 0 ]]; then
        log_success "🎉 NENHUM RECURSO CARO ATIVO - BUDGET PRESERVADO!"
        echo ""
        echo "Você pode sair tranquilo da sessão AWS Academy."
        echo "Lembre-se de executar este script no início da próxima sessão."
    else
        log_error "⚠️  RECURSOS CAROS AINDA ATIVOS:"
        [[ $active_eks -gt 0 ]] && echo "   • $active_eks cluster(s) EKS"
        [[ $running_ec2 -gt 0 ]] && echo "   • $running_ec2 instância(s) EC2"
        [[ $available_rds -gt 0 ]] && echo "   • $available_rds instância(s) RDS"
        [[ $active_lbs -gt 0 ]] && echo "   • $active_lbs Load Balancer(s)"
        [[ $active_nats -gt 0 ]] && echo "   • $active_nats NAT Gateway(s)"
        echo ""
        echo "Execute: ./scripts/cleanup-aws-academy.sh"
    fi
}

# Função principal
main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║           🔍 VERIFICAÇÃO DE RECURSOS CAROS - AWS ACADEMY       ║"
    echo "║                                                               ║"
    echo "║  Verifica se recursos que geram custos altos estão ativos     ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    echo ""
    
    check_aws_credentials
    
    check_eks
    check_ec2
    check_rds
    check_load_balancers
    check_nat_gateways
    
    show_summary
}

# Executar se chamado diretamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi