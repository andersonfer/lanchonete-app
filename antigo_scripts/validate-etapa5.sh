#!/bin/bash

set -e

echo "✅ VALIDAÇÃO COMPLETA DA ETAPA 5"
echo "==============================="

# Verificar estrutura das aplicações
echo "📁 1/5 - VERIFICANDO ESTRUTURA DAS APLICAÇÕES"
echo "--------------------------------------------"

if [ -d "applications/autoatendimento" ] && [ -d "applications/pagamento" ]; then
    echo "✅ Estrutura de diretórios criada"
else
    echo "❌ Estrutura de diretórios não encontrada"
    exit 1
fi

# Verificar POMs das aplicações
if [ -f "applications/autoatendimento/pom.xml" ] && [ -f "applications/pagamento/pom.xml" ]; then
    echo "✅ Arquivos pom.xml encontrados"
else
    echo "❌ Arquivos pom.xml não encontrados"
    exit 1
fi

# Verificar ApiGatewayContextFilter
echo ""
echo "🔧 2/5 - VERIFICANDO CONTEXT FILTER"
echo "-----------------------------------"

FILTER_FILE="applications/autoatendimento/src/main/java/br/com/lanchonete/autoatendimento/infra/ApiGatewayContextFilter.java"
if [ -f "$FILTER_FILE" ]; then
    echo "✅ ApiGatewayContextFilter.java encontrado"
    
    # Verificar se contém headers esperados
    if grep -q "X-Cliente-ID" "$FILTER_FILE" && grep -q "X-Auth-Type" "$FILTER_FILE"; then
        echo "✅ Headers de context injection implementados"
    else
        echo "❌ Headers de context injection não implementados"
        exit 1
    fi
else
    echo "❌ ApiGatewayContextFilter.java não encontrado"
    exit 1
fi

# Verificar Dockerfiles
echo ""
echo "🐳 3/5 - VERIFICANDO DOCKERFILES"
echo "--------------------------------"

if [ -f "applications/autoatendimento/Dockerfile" ] && [ -f "applications/pagamento/Dockerfile" ]; then
    echo "✅ Dockerfiles criados"
    
    # Verificar se são multi-stage builds
    if grep -q "FROM.*AS build" "applications/autoatendimento/Dockerfile"; then
        echo "✅ Multi-stage build configurado"
    else
        echo "❌ Multi-stage build não configurado"
        exit 1
    fi
else
    echo "❌ Dockerfiles não encontrados"
    exit 1
fi

# Verificar configurações Kubernetes
echo ""
echo "☸️ 4/5 - VERIFICANDO CONFIGURAÇÕES KUBERNETES"
echo "--------------------------------------------"

K8S_CONFIG_AUTO="applications/autoatendimento/src/main/resources/application-kubernetes.yml"
K8S_CONFIG_PAG="applications/pagamento/src/main/resources/application-kubernetes.yml"

if [ -f "$K8S_CONFIG_AUTO" ] && [ -f "$K8S_CONFIG_PAG" ]; then
    echo "✅ Configurações Kubernetes encontradas"
    
    # Verificar se contém configurações de RDS
    if grep -q "DB_HOST" "$K8S_CONFIG_AUTO"; then
        echo "✅ Configurações de RDS implementadas"
    else
        echo "❌ Configurações de RDS não encontradas"
        exit 1
    fi
else
    echo "❌ Configurações Kubernetes não encontradas"
    exit 1
fi

# Verificar scripts criados
echo ""
echo "📜 5/5 - VERIFICANDO SCRIPTS DE BUILD"
echo "------------------------------------"

SCRIPTS=("build-images.sh" "push-to-ecr.sh" "validate-apps.sh")
for script in "${SCRIPTS[@]}"; do
    if [ -f "scripts/$script" ] && [ -x "scripts/$script" ]; then
        echo "✅ $script criado e executável"
    else
        echo "❌ $script não encontrado ou não executável"
        exit 1
    fi
done

# Verificar dependências no autoatendimento
echo ""
echo "🔍 VERIFICAÇÃO ADICIONAL - DEPENDÊNCIAS"
echo "---------------------------------------"

if grep -q "spring-boot-starter-security" applications/autoatendimento/pom.xml; then
    echo "✅ Spring Security adicionado ao autoatendimento"
else
    echo "⚠️  Spring Security não encontrado no autoatendimento"
fi

if grep -q "mysql-connector-j" applications/autoatendimento/pom.xml; then
    echo "✅ MySQL connector configurado"
else
    echo "❌ MySQL connector não configurado"
    exit 1
fi

echo ""
echo "🎉 ETAPA 5 VALIDADA COM SUCESSO!"
echo "==============================="
echo "✅ Aplicações migradas da Fase 2"
echo "✅ ApiGatewayContextFilter implementado"
echo "✅ Dockerfiles multi-stage criados"
echo "✅ Configurações Kubernetes adaptadas"
echo "✅ Scripts de build e validação criados"
echo "✅ Dependências corretas configuradas"
echo ""
echo "🚀 PRONTO PARA PRÓXIMA ETAPA!"
echo "💡 Próximos passos:"
echo "   1. ./scripts/build-images.sh (testar build local)"
echo "   2. Prosseguir para ETAPA 6: Deploy Kubernetes"