#!/bin/bash

# Script para diagnosticar health checks (SEM comandos que não existem no Java container)
set -e

echo "🏥 DIAGNÓSTICO DE HEALTH CHECKS (VERSÃO CORRIGIDA)"
echo "=================================================="

# Função para testar endpoints (apenas com curl)
test_endpoints() {
    local pod_name=$1
    local port=$2
    local app_name=$3
    
    echo "🔍 Testando endpoints do $app_name (Pod: $pod_name)"
    
    # Testar endpoint básico
    echo "🩺 Testando /actuator/health..."
    if kubectl exec $pod_name -- curl -s --max-time 10 http://localhost:$port/actuator/health 2>/dev/null | grep -q "status"; then
        echo "✅ /actuator/health FUNCIONA"
        echo "📄 Response:"
        kubectl exec $pod_name -- curl -s http://localhost:$port/actuator/health | head -3
    else
        echo "❌ /actuator/health FALHA ou demora mais que 10s"
        echo "🔍 Tentando verificar se aplicação está rodando..."
        
        # Verificar se processo Java está rodando
        if kubectl exec $pod_name -- ps aux | grep -q java; then
            echo "✅ Processo Java está rodando"
        else
            echo "❌ Processo Java NÃO está rodando"
        fi
    fi
    
    # Testar endpoints específicos apenas se o básico funcionou
    echo "💓 Testando /actuator/health/liveness..."
    if kubectl exec $pod_name -- curl -s --max-time 5 http://localhost:$port/actuator/health/liveness 2>/dev/null | grep -q "status"; then
        echo "✅ /actuator/health/liveness FUNCIONA"
    else
        echo "❌ /actuator/health/liveness FALHA (404 ou timeout)"
    fi
    
    echo "✅ Testando /actuator/health/readiness..."
    if kubectl exec $pod_name -- curl -s --max-time 5 http://localhost:$port/actuator/health/readiness 2>/dev/null | grep -q "status"; then
        echo "✅ /actuator/health/readiness FUNCIONA"
    else
        echo "❌ /actuator/health/readiness FALHA (404 ou timeout)"
    fi
    
    # Listar todos os endpoints disponíveis
    echo "📋 Tentando listar endpoints disponíveis:"
    if kubectl exec $pod_name -- curl -s --max-time 5 http://localhost:$port/actuator 2>/dev/null | grep -q "_links"; then
        echo "✅ Conseguiu acessar /actuator"
        kubectl exec $pod_name -- curl -s http://localhost:$port/actuator | head -10
    else
        echo "❌ Não conseguiu acessar /actuator"
    fi
    
    # Verificar logs recentes da aplicação
    echo "📋 Logs recentes da aplicação:"
    kubectl logs $pod_name --tail=5 | grep -E "(Started|ERROR|Exception|Health)" || echo "Nenhum log relevante encontrado"
    
    echo "----------------------------------------"
}

# Verificar se kubectl está funcionando
if ! kubectl get pods >/dev/null 2>&1; then
    echo "❌ kubectl não está funcionando ou cluster não está acessível"
    exit 1
fi

# Testar autoatendimento
echo "🍔 TESTANDO AUTOATENDIMENTO"
if AUTOATENDIMENTO_POD=$(kubectl get pods -l app=autoatendimento --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null) && [ ! -z "$AUTOATENDIMENTO_POD" ]; then
    test_endpoints $AUTOATENDIMENTO_POD 8080 "autoatendimento"
else
    echo "❌ Nenhum Pod do autoatendimento RUNNING encontrado"
    echo "📋 Status dos Pods do autoatendimento:"
    kubectl get pods -l app=autoatendimento
fi

echo ""

# Testar pagamento
echo "💳 TESTANDO PAGAMENTO"
if PAGAMENTO_POD=$(kubectl get pods -l app=pagamento --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2>/dev/null) && [ ! -z "$PAGAMENTO_POD" ]; then
    test_endpoints $PAGAMENTO_POD 8081 "pagamento"
else
    echo "❌ Nenhum Pod do pagamento RUNNING encontrado"
    echo "📋 Status dos Pods do pagamento:"
    kubectl get pods -l app=pagamento
fi

echo ""
echo "📋 ANÁLISE GERAL DOS PODS:"
echo "=========================="
kubectl get pods -l 'app in (autoatendimento,pagamento)' -o wide

echo ""
echo "📋 RECOMENDAÇÕES BASEADAS NO DIAGNÓSTICO:"
echo "========================================="
echo ""
echo "SE /actuator/health FUNCIONOU em ambas as apps:"
echo "  → Use: kubectl apply -f k8s/deployments/autoatendimento-deployment-health-simple.yaml"
echo "  → Use: kubectl apply -f k8s/deployments/pagamento-deployment-health-simple.yaml"
echo ""
echo "SE /actuator/health FALHOU mas Java processo está rodando:"
echo "  → Use: kubectl apply -f k8s/deployments/autoatendimento-deployment-tcp-fallback.yaml"
echo ""
echo "SE NADA FUNCIONOU:"
echo "  → Primeiro corrija a configuração do Actuator e rebuild a aplicação"
echo "  → Depois teste novamente"
echo ""