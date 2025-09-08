#!/bin/bash

set -e

# Configurações
PROJECT_PREFIX="lanchonete"
LOG_FILE="/tmp/migration-$(date +%Y%m%d_%H%M%S).log"
RESPONSE_FILE="/tmp/migration-response.json"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

check_prerequisites() {
    log "=== VERIFICANDO PRÉ-REQUISITOS ==="
    
    # Verificar se AWS CLI está disponível
    if ! command -v aws &> /dev/null; then
        log "❌ AWS CLI não encontrado"
        exit 1
    fi
    
    # Verificar se está no diretório correto
    if [ ! -d "terraform/database" ]; then
        log "❌ Execute este script do diretório raiz do projeto (onde está a pasta terraform/)"
        log "   Diretório atual: $(pwd)"
        exit 1
    fi
    
    # Verificar se o database foi aplicado (remote backend S3)
    if [ ! -f "terraform/database/.terraform/terraform.tfstate" ]; then
        log "❌ Módulo database não foi inicializado. Execute 'terraform init' primeiro"
        exit 1
    fi
    
    log "✅ Pré-requisitos verificados"
}

get_terraform_outputs() {
    log "=== OBTENDO OUTPUTS DO TERRAFORM ==="
    
    cd terraform/database
    
    # Verificar se o módulo database foi aplicado (usando remote backend)
    if ! terraform show &>/dev/null; then
        log "❌ Módulo database não foi aplicado ainda ou remote state não está acessível"
        exit 1
    fi
    
    # Obter outputs do Terraform
    RDS_ENDPOINT=$(terraform output -raw rds_endpoint 2>/dev/null || echo "")
    DATABASE_NAME=$(terraform output -raw database_name 2>/dev/null || echo "")
    MIGRATION_LAMBDA="${PROJECT_PREFIX}-migration"
    MIGRATIONS_BUCKET=$(terraform output -raw migrations_bucket 2>/dev/null || echo "")
    
    cd - > /dev/null
    
    if [ -z "$RDS_ENDPOINT" ] || [ -z "$DATABASE_NAME" ]; then
        log "❌ Não foi possível obter outputs do Terraform"
        log "RDS_ENDPOINT: $RDS_ENDPOINT"
        log "DATABASE_NAME: $DATABASE_NAME"
        log "MIGRATION_LAMBDA: $MIGRATION_LAMBDA"
        exit 1
    fi
    
    log "✅ Outputs obtidos:"
    log "   RDS Endpoint: $RDS_ENDPOINT"
    log "   Migration Lambda: $MIGRATION_LAMBDA"
    log "   Migrations Bucket: $MIGRATIONS_BUCKET"
}

check_lambda_exists() {
    log "=== VERIFICANDO SE LAMBDA EXISTE ==="
    
    if aws lambda get-function --function-name "$MIGRATION_LAMBDA" &>/dev/null; then
        log "✅ Lambda $MIGRATION_LAMBDA encontrada"
    else
        log "❌ Lambda $MIGRATION_LAMBDA não encontrada"
        exit 1
    fi
}

check_rds_status() {
    log "=== VERIFICANDO STATUS DO RDS ==="
    
    local db_identifier=$(echo "$RDS_ENDPOINT" | cut -d'.' -f1)
    local status=$(aws rds describe-db-instances --db-instance-identifier "$db_identifier" --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null || echo "not-found")
    
    if [ "$status" = "available" ]; then
        log "✅ RDS $db_identifier está disponível"
    else
        log "❌ RDS $db_identifier status: $status (precisa estar 'available')"
        exit 1
    fi
}

run_migration() {
    log "=== EXECUTANDO MIGRATIONS ==="
    
    log "🚀 Invocando Lambda de migration..."
    
    # Limpar arquivo de resposta anterior
    rm -f "$RESPONSE_FILE"
    
    # Invocar Lambda
    if aws lambda invoke --function-name "$MIGRATION_LAMBDA" "$RESPONSE_FILE" --log-type Tail --query 'LogResult' --output text | base64 --decode > "${RESPONSE_FILE}.logs" 2>/dev/null; then
        log "📄 Lambda invocada com sucesso"
        
        # Mostrar logs da Lambda
        if [ -f "${RESPONSE_FILE}.logs" ]; then
            log "📋 Logs da Lambda:"
            cat "${RESPONSE_FILE}.logs" | while IFS= read -r line; do
                log "   $line"
            done
        fi
        
        # Verificar resposta
        if [ -f "$RESPONSE_FILE" ]; then
            log "📄 Resposta da migration:"
            cat "$RESPONSE_FILE" | tee -a "$LOG_FILE"
            echo
            
            # Verificar se foi bem-sucedida
            if grep -q '"status":"success"' "$RESPONSE_FILE"; then
                log "✅ Migrations executadas com sucesso!"
                return 0
            else
                log "❌ Migration falhou - verifique os logs acima"
                return 1
            fi
        else
            log "❌ Arquivo de resposta não foi criado"
            return 1
        fi
    else
        log "❌ Falha ao invocar Lambda"
        return 1
    fi
}

cleanup() {
    log "=== LIMPANDO ARQUIVOS TEMPORÁRIOS ==="
    rm -f "$RESPONSE_FILE" "${RESPONSE_FILE}.logs"
}

show_help() {
    echo "Usage: $0 [options]"
    echo ""
    echo "Executa migrations do banco de dados via Lambda após terraform apply"
    echo ""
    echo "Options:"
    echo "  --dry-run    Apenas verifica se tudo está pronto, mas não executa migration"
    echo "  --force      Force a execução mesmo se migration já foi executada"
    echo "  --help, -h   Mostra esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0                    # Execução normal"
    echo "  $0 --dry-run         # Apenas verificar"
    echo "  $0 --force           # Forçar re-execução"
}

main() {
    local DRY_RUN=false
    local FORCE=false
    
    # Parse argumentos
    while [[ $# -gt 0 ]]; do
        case $1 in
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --force)
                FORCE=true
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                echo "Opção desconhecida: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    log "🚀 Iniciando script de migrations"
    log "Log file: $LOG_FILE"
    
    check_prerequisites
    get_terraform_outputs
    check_lambda_exists
    check_rds_status
    
    if [ "$DRY_RUN" = true ]; then
        log "🔍 DRY RUN MODE - Tudo está pronto para executar migrations"
        log "Execute sem --dry-run para executar as migrations"
        exit 0
    fi
    
    if run_migration; then
        log "🎉 Script de migrations concluído com sucesso!"
        log "📋 Log completo: $LOG_FILE"
    else
        log "💥 Script de migrations falhou!"
        log "📋 Verifique o log: $LOG_FILE"
        exit 1
    fi
    
    cleanup
}

# Tratamento de sinais para cleanup
trap cleanup EXIT INT TERM

main "$@"