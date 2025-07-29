#!/bin/bash

# Script para demonstrar HPA do Autoatendimento
# Cria carga controlada para escalar de 2 para 4 pods

set -e

echo "🚀 DEMONSTRAÇÃO DE HPA - AUTOATENDIMENTO"
echo "======================================="
echo ""

# Verificar se HPA está configurado
if ! kubectl get hpa autoatendimento-hpa >/dev/null 2>&1; then
    echo "❌ HPA não encontrado! Execute primeiro:"
    echo "kubectl apply -f autoatendimento-hpa.yaml"
    exit 1
fi

echo "📊 ESTADO INICIAL:"
echo "=================="
echo "HPA Status:"
kubectl get hpa autoatendimento-hpa
echo ""
echo "Pods atuais:"
kubectl get pods -l app=autoatendimento
echo ""
echo "Consumo de CPU atual:"
kubectl top pods -l app=autoatendimento
echo ""

read -p "▶️  Pressione ENTER para iniciar teste de 5 minutos..."

echo ""
echo "🔥 INICIANDO TESTE DE CARGA"
echo "==========================="
echo "Duração: 5 minutos total"
echo "Target: 60% CPU (vs atual 6%)"
echo "Esperado: Scale up em ~1-2 minutos"
echo ""
echo "💡 Abra outro terminal e execute:"
echo "watch 'kubectl get hpa autoatendimento-hpa; echo; kubectl get pods -l app=autoatendimento'"
echo ""

# Criar gerador de carga otimizado
cat << 'EOF' | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: load-generator-hpa
  labels:
    app: load-test
spec:
  containers:
  - name: busybox
    image: busybox:latest
    command: ["sh", "-c"]
    args:
    - |
      echo "🔥 INICIANDO GERADOR DE CARGA PARA HPA"
      echo "Target: CPU 60% para escalar 2→4 pods"
      echo "====================================="
      
      # Função para gerar carga (SILENCIOSA)
      generate_load() {
        local duration=$1
        local concurrent=$2
        echo "⚡ Gerando carga: $concurrent requisições simultâneas por $duration segundos"
        
        local end_time=$(($(date +%s) + duration))
        local progress_interval=10
        local last_progress=0
        
        while [ $(date +%s) -lt $end_time ]; do
          # Requisições SILENCIOSAS
          for i in $(seq 1 $concurrent); do
            wget -q -O- "http://autoatendimento-service:8080/produtos/categoria/LANCHE" >/dev/null 2>&1 &
            wget -q -O- "http://autoatendimento-service:8080/actuator/health" >/dev/null 2>&1 &
          done
          sleep 0.5
          
          # Mostrar progresso apenas a cada 10 segundos
          local current_time=$(date +%s)
          local elapsed=$((current_time - (end_time - duration)))
          
          if [ $((elapsed - last_progress)) -ge $progress_interval ]; then
            local remaining=$((end_time - current_time))
            echo "   📊 Progresso: ${elapsed}s/${duration}s - Restam: ${remaining}s"
            last_progress=$elapsed
          fi
        done
        
        # Aguardar requisições terminarem (silenciosamente)
        wait >/dev/null 2>&1
        echo "   ✅ Fase concluída!"
      }
      
      # FASE 1: Carga leve (warm-up)
      echo ""
      echo "📈 FASE 1: Warm-up (30s)"
      generate_load 30 3
      
      # FASE 2: Carga média 
      echo ""
      echo "📈 FASE 2: Carga moderada (60s)"
      generate_load 60 8
      
      # FASE 3: Carga alta (para trigger scaling)
      echo ""
      echo "🔥 FASE 3: CARGA ALTA - TRIGGER SCALING! (90s)"
      generate_load 90 15
      
      # FASE 4: Manter carga para estabilizar scaling
      echo ""
      echo "🎯 FASE 4: Manter escala (60s)"
      generate_load 60 10
      
      # FASE 5: Reduzir carga (scale down)
      echo ""
      echo "📉 FASE 5: Redução - preparar scale down (60s)"
      generate_load 60 2
      
      echo ""
      echo "✅ GERADOR DE CARGA FINALIZADO!"
      
  restartPolicy: Never
EOF

echo "✅ Gerador de carga criado!"
echo ""
echo "📊 MONITORAMENTO PARALELO (OPCIONAL):"
echo "====================================="
echo "Para acompanhar em outro terminal:"
echo "kubectl get hpa -w"
echo ""

# Monitorar logs do teste
echo "📋 AGUARDANDO POD DE TESTE FICAR PRONTO:"
echo "========================================"

# Aguardar pod ficar pronto antes de tentar logs
echo "⏳ Aguardando load-generator-hpa ficar Running..."
if kubectl wait --for=condition=ready pod/load-generator-hpa --timeout=120s >/dev/null 2>&1; then
    echo "✅ Pod pronto! Monitorando teste de 5 minutos..."
    echo ""
    
    # Mostrar progresso enquanto aguarda (TEMPO CORRETO)
    echo ""
    echo "📊 MONITORAMENTO DO TESTE (5 min total, check a cada 30s):"
    echo "=========================================================="
    
    # 10 iterações de 30s = 300s = 5 minutos
    for i in {1..10}; do
        sleep 30
        
        # Mostrar progresso mais claro (checks a cada 30s por 5 min)
        echo ""
        echo "🕐 Check $i/10 (Teste de 5 minutos):"
        echo "------------------------------------"
        
        # Status do HPA (formatado)
        HPA_STATUS=$(kubectl get hpa autoatendimento-hpa --no-headers 2>/dev/null || echo "HPA carregando...")
        echo "📈 HPA: $HPA_STATUS"
        
        # Contagem de pods
        POD_COUNT=$(kubectl get pods -l app=autoatendimento --no-headers 2>/dev/null | wc -l)
        echo "🏗️  Pods: $POD_COUNT"
        
        # CPU médio atual (se disponível)
        AVG_CPU=$(kubectl top pods -l app=autoatendimento --no-headers 2>/dev/null | awk '{sum+=$2} END {if(NR>0) printf "%.0f", sum/NR; else print "N/A"}' | sed 's/m$//')
        if [ "$AVG_CPU" != "N/A" ] && [ ! -z "$AVG_CPU" ]; then
            echo "💻 CPU médio: ${AVG_CPU}m"
        fi
        
        # Verificar se teste ainda está rodando
        if ! kubectl get pod load-generator-hpa >/dev/null 2>&1; then
            echo ""
            echo "✅ TESTE TERMINOU!"
            break
        fi
        
        POD_STATUS=$(kubectl get pod load-generator-hpa -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
        if [ "$POD_STATUS" = "Succeeded" ] || [ "$POD_STATUS" = "Failed" ]; then
            echo ""
            echo "✅ TESTE TERMINOU - Status: $POD_STATUS"
            break
        fi
        
        # Mostrar em que fase está baseado no tempo (mais específico)
        if [ "$i" -eq 1 ]; then
            echo "📊 Fase: Warm-up (0:30)"
        elif [ "$i" -eq 2 ]; then
            echo "📊 Fase: Warm-up → Carga moderada (1:00)"
        elif [ "$i" -eq 3 ]; then
            echo "📊 Fase: Carga moderada (1:30)"
        elif [ "$i" -eq 4 ]; then
            echo "📊 Fase: Carga moderada → Alta (2:00)"
        elif [ "$i" -eq 5 ]; then
            echo "📊 Fase: 🔥 CARGA ALTA - scaling! (2:30)"
        elif [ "$i" -eq 6 ]; then
            echo "📊 Fase: 🔥 CARGA ALTA - scaling! (3:00)"
        elif [ "$i" -eq 7 ]; then
            echo "📊 Fase: Manter escala (3:30)"
        elif [ "$i" -eq 8 ]; then
            echo "📊 Fase: Manter escala (4:00)"
        elif [ "$i" -eq 9 ]; then
            echo "📊 Fase: 📉 Redução - prep scale down (4:30)"
        else
            echo "📊 Fase: 📉 Redução final (5:00)"
        fi
        
        echo "🔄 Teste em progresso..."
    done
    
else
    echo "❌ Pod não ficou pronto em 2 minutos"
    echo "⚠️  Teste pode estar rodando. Aguardando 1 minuto adicional..."
    sleep 60
fi

# Aguardar teste terminar (verificação final silenciosa)
echo ""
echo "⏳ Finalizando teste de 5 minutos..."
for i in {1..3}; do
    if ! kubectl get pod load-generator-hpa >/dev/null 2>&1; then
        break
    fi
    
    POD_STATUS=$(kubectl get pod load-generator-hpa -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
    if [ "$POD_STATUS" = "Succeeded" ] || [ "$POD_STATUS" = "Failed" ]; then
        break
    fi
    
    sleep 30
done

echo ""
echo "🎉 RESULTADO FINAL DO TESTE (5 minutos):"
echo "========================================"
kubectl get hpa autoatendimento-hpa
echo ""
FINAL_PODS=$(kubectl get pods -l app=autoatendimento --no-headers | wc -l)
echo "📊 Pods finais: $FINAL_PODS"
echo ""

# Limpar
echo "🧹 Limpando recursos de teste..."
kubectl delete pod load-generator-hpa --ignore-not-found=true >/dev/null 2>&1

echo ""
echo "✅ DEMO HPA CONCLUÍDA! (5 minutos)"
echo "=================================="
echo ""
echo "🎯 PRÓXIMOS PASSOS:"
echo "• Scale down automático: aguardar 3-5 min (se CPU baixo)"
echo "• Monitorar: kubectl get hpa -w"
echo "• Repetir teste (5 min): bash teste-carga-hpa.sh"
