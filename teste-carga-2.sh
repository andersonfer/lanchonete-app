#!/bin/bash
# ========================================
# TESTE DE CARGA SUAVE PARA HPA
# ========================================

echo "🎯 Iniciando teste de carga SUAVE para HPA..."

# Verificar se serviço está acessível primeiro
echo "🔍 Verificando conectividade..."
if ! curl -s --max-time 5 http://localhost:30080/actuator/health > /dev/null; then
    echo "❌ Serviço não acessível em localhost:30080"
    echo "🔄 Tentando port-forward..."
    kubectl port-forward service/autoatendimento-service 8080:8080 &
    PORT_FORWARD_PID=$!
    sleep 3
    BASE_URL="http://localhost:8080"
    echo "✅ Usando port-forward: $BASE_URL"
else
    BASE_URL="http://localhost:30080"
    echo "✅ Usando NodePort: $BASE_URL"
fi

# Função para enviar requisições controladas
send_requests() {
    local count=$1
    local delay=$2
    echo "📤 Enviando $count requisições com delay de ${delay}s..."
    
    for i in $(seq 1 $count); do
        echo "  Requisição $i/$count..."
        curl -s -X POST "$BASE_URL/clientes" \
            -H "Content-Type: application/json" \
            -d "{\"nome\":\"Cliente$i\",\"email\":\"cliente$i@test.com\",\"cpf\":\"123.456.789-$(printf "%02d" $i)\"}" \
            --max-time 10 > /dev/null
        
        if [ $? -eq 0 ]; then
            echo "    ✅ Sucesso"
        else
            echo "    ❌ Falha (timeout ou erro)"
        fi
        
        sleep $delay
        
        # Verificar HPA a cada 5 requisições
        if [ $((i % 5)) -eq 0 ]; then
            echo "  📊 Status HPA:"
            kubectl get hpa autoatendimento-hpa --no-headers 2>/dev/null || echo "    ❌ Erro ao obter HPA"
        fi
    done
}

# Monitorar recursos antes do teste
echo "📊 Status inicial:"
kubectl get hpa autoatendimento-hpa --no-headers 2>/dev/null
kubectl top pods -l app=autoatendimento 2>/dev/null

echo ""
echo "🚀 Iniciando teste gradual..."

# FASE 1: Teste leve (5 req, 2s delay)
echo "📈 FASE 1: Carga leve..."
send_requests 5 2

echo ""
echo "⏸️  Pausa de 10 segundos..."
sleep 10

# FASE 2: Teste moderado (10 req, 1s delay)  
echo "📈 FASE 2: Carga moderada..."
send_requests 10 1

echo ""
echo "⏸️  Pausa de 15 segundos..."
sleep 15

# FASE 3: Teste intenso (20 req, 0.5s delay)
echo "📈 FASE 3: Carga intensa..."
send_requests 20 0.5

echo ""
echo "📊 Status final:"
kubectl get hpa autoatendimento-hpa 2>/dev/null
kubectl top pods -l app=autoatendimento 2>/dev/null

# Cleanup port-forward se foi usado
if [ ! -z "$PORT_FORWARD_PID" ]; then
    echo "🧹 Limpando port-forward..."
    kill $PORT_FORWARD_PID 2>/dev/null
fi

echo "✅ Teste concluído!"
