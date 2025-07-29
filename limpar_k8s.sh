#!/bin/bash

# Script de limpeza robusto para Tech Challenge Fase 2
# Remove todos os recursos do projeto de forma segura e ordenada

set -e

echo "🧹 INICIANDO LIMPEZA COMPLETA DO TECH CHALLENGE FASE 2"
echo "======================================================"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para imprimir com cores
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }

# Função para aguardar recurso ser deletado
wait_for_deletion() {
    local resource_type=$1
    local resource_name=$2
    local timeout=${3:-60}
    local elapsed=0
    local check_interval=2
    
    print_info "Aguardando deleção de $resource_type/$resource_name..."
    
    while kubectl get $resource_type $resource_name >/dev/null 2>&1; do
        if [ $elapsed -ge $timeout ]; then
            print_warning "Timeout aguardando deleção de $resource_type/$resource_name"
            return 1
        fi
        sleep $check_interval
        elapsed=$((elapsed + check_interval))
        echo -n "."
    done
    echo ""
    print_success "$resource_type/$resource_name deletado!"
    return 0
}

# Função para deletar recurso com retry
delete_resource() {
    local resource_type=$1
    local resource_name=$2
    local force=${3:-false}
    
    if ! kubectl get $resource_type $resource_name >/dev/null 2>&1; then
        print_info "$resource_type/$resource_name já não existe"
        return 0
    fi
    
    print_info "Deletando $resource_type/$resource_name..."
    
    if [ "$force" = "true" ]; then
        kubectl delete $resource_type $resource_name --force --grace-period=0 2>/dev/null || true
    else
        kubectl delete $resource_type $resource_name 2>/dev/null || true
    fi
    
    # Aguardar deleção com timeout
    if wait_for_deletion $resource_type $resource_name 30; then
        return 0
    else
        print_warning "Forçando deleção de $resource_type/$resource_name..."
        kubectl delete $resource_type $resource_name --force --grace-period=0 2>/dev/null || true
        sleep 2
        return 0
    fi
}

# Função para deletar recursos por label
delete_by_label() {
    local resource_type=$1
    local label_selector=$2
    local force=${3:-false}
    
    local resources=$(kubectl get $resource_type -l "$label_selector" -o name 2>/dev/null || true)
    
    if [ -z "$resources" ]; then
        print_info "Nenhum $resource_type encontrado com label $label_selector"
        return 0
    fi
    
    print_info "Deletando $resource_type com label $label_selector..."
    
    if [ "$force" = "true" ]; then
        kubectl delete $resource_type -l "$label_selector" --force --grace-period=0 2>/dev/null || true
    else
        kubectl delete $resource_type -l "$label_selector" 2>/dev/null || true
    fi
    
    # Aguardar até todos serem deletados
    local timeout=60
    local elapsed=0
    while [ $elapsed -lt $timeout ]; do
        local remaining=$(kubectl get $resource_type -l "$label_selector" --no-headers 2>/dev/null | wc -l)
        if [ "$remaining" -eq 0 ]; then
            print_success "Todos os $resource_type deletados!"
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
        echo -n "."
    done
    
    # Force delete se ainda existir
    print_warning "Forçando deleção dos $resource_type restantes..."
    kubectl delete $resource_type -l "$label_selector" --force --grace-period=0 2>/dev/null || true
    sleep 2
}

# Verificar se kubectl está funcionando
if ! kubectl get nodes >/dev/null 2>&1; then
    print_error "kubectl não está funcionando ou cluster não está acessível"
    exit 1
fi

print_success "kubectl funcionando!"
echo ""

# FASE 1: Remover HPAs primeiro (evita recriação de pods)
print_info "📊 FASE 1: REMOVENDO HPAs"
echo "========================="
delete_by_label "hpa" "app in (autoatendimento,pagamento)"
echo ""

# FASE 2: Remover Deployments e StatefulSets (para parar pods)
print_info "🏭 FASE 2: REMOVENDO DEPLOYMENTS E STATEFULSETS" 
echo "=============================================="
delete_by_label "deployment" "app in (autoatendimento,pagamento)"
delete_by_label "statefulset" "app=mysql"
echo ""

# FASE 3: Aguardar todos os pods terminarem
print_info "⏳ FASE 3: AGUARDANDO PODS TERMINAREM"
echo "===================================="
timeout=120
elapsed=0
while [ $elapsed -lt $timeout ]; do
    remaining_pods=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --no-headers 2>/dev/null | grep -v Terminating | wc -l)
    if [ "$remaining_pods" -eq 0 ]; then
        print_success "Todos os pods foram terminados!"
        break
    fi
    print_info "Aguardando $remaining_pods pods terminarem..."
    sleep 5
    elapsed=$((elapsed + 5))
done

# Force delete pods restantes
remaining_pods=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --no-headers 2>/dev/null | wc -l)
if [ "$remaining_pods" -gt 0 ]; then
    print_warning "Forçando deleção de pods restantes..."
    kubectl delete pods -l 'app in (autoatendimento,pagamento,mysql)' --force --grace-period=0 2>/dev/null || true
fi
echo ""

# FASE 4: Remover Services (agora que pods não existem mais)
print_info "🌐 FASE 4: REMOVENDO SERVICES"
echo "============================="

# Deletar services específicos por nome (mais confiável)
for service in autoatendimento-service pagamento-service mysql-service mysql-headless-service; do
    delete_resource "service" "$service"
done

# Verificar se algum service ainda resta
remaining_services=$(kubectl get services | grep -E "(autoatendimento|pagamento|mysql)" | wc -l)
if [ "$remaining_services" -gt 0 ]; then
    print_warning "Deletando services restantes por label..."
    delete_by_label "service" "app in (autoatendimento,pagamento,mysql)" true
fi
echo ""

# FASE 5: Remover PVCs (antes dos PVs)
print_info "💾 FASE 5: REMOVENDO PVCs"
echo "========================="
delete_by_label "pvc" "app=mysql"
echo ""

# FASE 6: Remover PVs (mais complexo devido aos finalizers)
print_info "🗄️  FASE 6: REMOVENDO PVs"
echo "========================"

# Listar PVs relacionados ao projeto
project_pvs=$(kubectl get pv -o name | grep -E "(mysql|lanchonete)" || true)

for pv_name in $project_pvs; do
    pv_short_name=$(echo $pv_name | sed 's/persistentvolume\///')
    
    print_info "Processando $pv_short_name..."
    
    # Verificar se PV existe
    if kubectl get pv $pv_short_name >/dev/null 2>&1; then
        # Remover claimRef se existir
        kubectl patch pv $pv_short_name -p '{"spec":{"claimRef":null}}' 2>/dev/null || true
        
        # Tentar deleção normal primeiro
        kubectl delete pv $pv_short_name 2>/dev/null || true
        
        # Aguardar ou forçar
        if ! wait_for_deletion "pv" "$pv_short_name" 30; then
            print_warning "Removendo finalizers de $pv_short_name..."
            kubectl patch pv $pv_short_name -p '{"metadata":{"finalizers":null}}' --type=merge 2>/dev/null || true
            sleep 2
        fi
    fi
done
echo ""

# FASE 7: Remover ConfigMaps e Secrets
print_info "⚙️  FASE 7: REMOVENDO CONFIGMAPS E SECRETS"
echo "=========================================="
delete_by_label "configmap" "app in (autoatendimento,pagamento,mysql)"
delete_by_label "secret" "app=mysql"

# Deletar secrets específicos por nome também
for secret in mysql-secret; do
    delete_resource "secret" "$secret"
done
echo ""

# FASE 8: Verificação final e limpeza de recursos órfãos
print_info "🔍 FASE 8: VERIFICAÇÃO FINAL"
echo "============================"

# Listar recursos órfãos
print_info "Verificando recursos órfãos..."
orphaned_resources=$(kubectl get all -l 'app in (autoatendimento,pagamento,mysql)' 2>/dev/null || true)

if [ ! -z "$orphaned_resources" ] && [ "$(echo "$orphaned_resources" | wc -l)" -gt 1 ]; then
    print_warning "Recursos órfãos encontrados:"
    echo "$orphaned_resources"
    print_info "Removendo recursos órfãos..."
    kubectl delete all -l 'app in (autoatendimento,pagamento,mysql)' --force --grace-period=0 2>/dev/null || true
fi

# Deletar endpoints órfãos
kubectl delete endpoints autoatendimento-service pagamento-service mysql-service mysql-headless-service 2>/dev/null || true

echo ""
print_info "📊 VERIFICAÇÃO DE LIMPEZA FINAL"
echo "==============================="

# Contadores finais
pods_count=$(kubectl get pods -l 'app in (autoatendimento,pagamento,mysql)' --no-headers 2>/dev/null | wc -l)
services_count=$(kubectl get services --no-headers | grep -E "(autoatendimento|pagamento|mysql)" | wc -l)
pvs_count=$(kubectl get pv --no-headers | grep -E "(mysql|lanchonete)" | wc -l)
pvcs_count=$(kubectl get pvc -l 'app=mysql' --no-headers 2>/dev/null | wc -l)

echo "Pods restantes: $pods_count"
echo "Services restantes: $services_count"  
echo "PVs restantes: $pvs_count"
echo "PVCs restantes: $pvcs_count"

# Status final
if [ "$pods_count" -eq 0 ] && [ "$services_count" -eq 0 ] && [ "$pvs_count" -eq 0 ] && [ "$pvcs_count" -eq 0 ]; then
    echo ""
    print_success "🎉 LIMPEZA CONCLUÍDA COM SUCESSO!"
    print_success "Todos os recursos do projeto foram removidos."
else
    echo ""
    print_warning "⚠️  Limpeza parcial - alguns recursos ainda existem"
    print_info "Execute 'kubectl get all' para verificar recursos restantes"
fi

echo ""
print_info "💡 Para verificar o ambiente limpo:"
echo "kubectl get all"
echo "kubectl get pv,pvc"
echo "kubectl get secrets,configmaps"
