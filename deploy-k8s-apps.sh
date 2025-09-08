#!/bin/bash

set -e

PROJECT_PREFIX="lanchonete"
LOG_FILE="/tmp/k8s-deploy-$(date +%Y%m%d_%H%M%S).log"
TEMP_DIR="/tmp/k8s-manifests-$$"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

cleanup() {
    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
        log "🧹 Temporary files cleaned up"
    fi
}

trap cleanup EXIT

log "🚀 Iniciando deploy das aplicações Kubernetes"
log "Log file: $LOG_FILE"

# Verificar pré-requisitos
log "=== VERIFICANDO PRÉ-REQUISITOS ==="

# Verificar se kubectl está configurado
if ! kubectl cluster-info >/dev/null 2>&1; then
    log "❌ kubectl não está configurado ou cluster não acessível"
    exit 1
fi

# Verificar se os manifests existem
if [ ! -d "k8s" ]; then
    log "❌ Diretório k8s/ não encontrado"
    exit 1
fi

log "✅ Pré-requisitos verificados"

# Obter outputs do Terraform
log "=== OBTENDO OUTPUTS DO TERRAFORM ==="

# Obter outputs do módulo database
log "📊 Obtendo outputs do módulo database..."
cd terraform/database
DB_OUTPUTS=$(terraform output -json 2>/dev/null || {
    log "❌ Erro ao obter outputs do módulo database"
    exit 1
})

RDS_ENDPOINT=$(echo "$DB_OUTPUTS" | jq -r '.rds_endpoint_host.value // empty')
DB_NAME=$(echo "$DB_OUTPUTS" | jq -r '.database_name.value // empty')

if [ -z "$RDS_ENDPOINT" ]; then
    log "❌ RDS endpoint não encontrado nos outputs"
    exit 1
fi

log "✅ RDS Endpoint: $RDS_ENDPOINT"
log "✅ Database Name: $DB_NAME"

# Obter outputs do módulo kubernetes
cd ../kubernetes
log "📊 Obtendo outputs do módulo kubernetes..."
K8S_OUTPUTS=$(terraform output -json 2>/dev/null || {
    log "❌ Erro ao obter outputs do módulo kubernetes"
    exit 1
})

ECR_AUTOATENDIMENTO_URI=$(echo "$K8S_OUTPUTS" | jq -r '.ecr_autoatendimento_url.value // empty')
ECR_PAGAMENTO_URI=$(echo "$K8S_OUTPUTS" | jq -r '.ecr_pagamento_url.value // empty')

if [ -z "$ECR_AUTOATENDIMENTO_URI" ] || [ -z "$ECR_PAGAMENTO_URI" ]; then
    log "❌ ECR URIs não encontradas nos outputs"
    exit 1
fi

log "✅ ECR Autoatendimento: $ECR_AUTOATENDIMENTO_URI"
log "✅ ECR Pagamento: $ECR_PAGAMENTO_URI"

# Obter credenciais do terraform.tfvars
cd ../../
log "📊 Obtendo credenciais do terraform.tfvars..."

if [ ! -f "terraform/shared/terraform.tfvars" ]; then
    log "❌ Arquivo terraform.tfvars não encontrado"
    exit 1
fi

DB_USERNAME=$(grep '^db_username' terraform/shared/terraform.tfvars | cut -d'"' -f2)
DB_PASSWORD=$(grep '^db_password' terraform/shared/terraform.tfvars | cut -d'"' -f2)

if [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    log "❌ Credenciais do banco não encontradas no terraform.tfvars"
    exit 1
fi

log "✅ Credenciais obtidas (username: $DB_USERNAME)"

# Criar diretório temporário para manifests processados
mkdir -p "$TEMP_DIR"
cp -r k8s/* "$TEMP_DIR/"

log "=== PROCESSANDO MANIFESTS ==="

# Processar ConfigMaps
log "📝 Processando configmaps..."

# Autoatendimento ConfigMap - conectar ao RDS
sed -i "s|DB_HOST: \"mysql-service\"|DB_HOST: \"$RDS_ENDPOINT\"|g" "$TEMP_DIR/configmaps/autoatendimento-configmap.yaml"
sed -i "s|SPRING_SQL_INIT_MODE: \"always\"|SPRING_SQL_INIT_MODE: \"never\"|g" "$TEMP_DIR/configmaps/autoatendimento-configmap.yaml"

log "✅ ConfigMap autoatendimento processado (RDS: $RDS_ENDPOINT)"

# Processar Deployments
log "📝 Processando deployments..."

# Autoatendimento Deployment - usar imagem ECR
sed -i "s|image: lanchonete-app-autoatendimento:latest|image: $ECR_AUTOATENDIMENTO_URI:latest|g" "$TEMP_DIR/deployments/autoatendimento-deployment.yaml"
sed -i "s|imagePullPolicy: Never|imagePullPolicy: Always|g" "$TEMP_DIR/deployments/autoatendimento-deployment.yaml"

# Pagamento Deployment - usar imagem ECR
sed -i "s|image: lanchonete-app-pagamento:latest|image: $ECR_PAGAMENTO_URI:latest|g" "$TEMP_DIR/deployments/pagamento-deployment.yaml"
sed -i "s|imagePullPolicy: Never|imagePullPolicy: Always|g" "$TEMP_DIR/deployments/pagamento-deployment.yaml"

log "✅ Deployments processados com imagens ECR"

# Criar/atualizar secrets dinamicamente
log "=== CRIANDO SECRETS PARA RDS ==="

kubectl create secret generic mysql-secret \
  --from-literal=MYSQL_ROOT_PASSWORD="$DB_PASSWORD" \
  --from-literal=MYSQL_USER="$DB_USERNAME" \
  --from-literal=MYSQL_PASSWORD="$DB_PASSWORD" \
  --from-literal=DB_USERNAME="$DB_USERNAME" \
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -

log "✅ Secret mysql-secret criado/atualizado para RDS"

# Aplicar manifests processados
log "=== APLICANDO MANIFESTS ==="

# Aplicar ConfigMaps
log "📋 Aplicando configmaps..."
kubectl apply -f "$TEMP_DIR/configmaps/"
log "✅ ConfigMaps aplicados"

# Aplicar Services
log "📋 Aplicando services..."
kubectl apply -f "$TEMP_DIR/services/"
log "✅ Services aplicados"

# Aplicar Deployments
log "📋 Aplicando deployments..."
kubectl apply -f "$TEMP_DIR/deployments/"
log "✅ Deployments aplicados"

# Aguardar um pouco e verificar status
log "=== VERIFICANDO STATUS ==="

log "📊 Aguardando 15 segundos para estabilização..."
sleep 15

log "📊 Status dos deployments:"
kubectl get deployments -o wide

log "📊 Status dos pods:"
kubectl get pods -l 'app in (autoatendimento,pagamento)' -o wide

log "📊 Status dos services:"
kubectl get services -l component=service -o wide

log "📊 Testando conectividade com RDS..."
# Mostrar logs recentes do autoatendimento (que conecta no RDS)
kubectl logs -l app=autoatendimento --tail=10 2>/dev/null || log "⚠️ Pods ainda iniciando..."

log "🎉 Deploy das aplicações Kubernetes concluído com sucesso!"
log "📋 Log completo: $LOG_FILE"

log "=== PRÓXIMOS PASSOS ==="
log "1. Monitorar pods: kubectl get pods -w -l 'app in (autoatendimento,pagamento)'"
log "2. Ver logs autoatendimento: kubectl logs -f deployment/autoatendimento-deployment"
log "3. Ver logs pagamento: kubectl logs -f deployment/pagamento-deployment" 
log "4. Testar via NLB: curl http://lanchonete-nlb-xxx.elb.amazonaws.com:30080/produtos/categoria/LANCHE"
log "5. Verificar health checks: kubectl describe pod -l app=autoatendimento"

log "🔗 **IMPORTANTE**: As aplicações agora conectam diretamente ao RDS externo!"
log "🔗 RDS Endpoint configurado: $RDS_ENDPOINT"