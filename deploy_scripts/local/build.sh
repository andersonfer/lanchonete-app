#!/bin/bash
set -e

echo "🔨 Building images for MINIKUBE..."
echo ""

# Verifica se está no contexto do Minikube
CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
if [[ "$CURRENT_CONTEXT" != "minikube" ]]; then
    echo "⚠️  Warning: Not in Minikube context (current: $CURRENT_CONTEXT)"
    echo "   Continuing anyway, but make sure Minikube is running..."
    echo ""
fi

# Configura Docker para usar o daemon do Minikube
echo "📌 Configuring Docker to use Minikube daemon..."
eval $(minikube docker-env)

# Lista de serviços
SERVICES=("clientes" "pedidos" "cozinha" "pagamento")

# Build de cada serviço
for service in "${SERVICES[@]}"; do
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 Building: $service"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    SERVICE_DIR="services/$service"

    if [[ ! -d "$SERVICE_DIR" ]]; then
        echo "❌ Error: Directory $SERVICE_DIR not found!"
        exit 1
    fi

    # Build Maven
    echo "🔧 Running Maven build..."
    cd "$SERVICE_DIR"
    mvn clean package -DskipTests -q

    # Build Docker
    echo "🐳 Building Docker image..."
    docker build -t "lanchonete-$service:latest" .

    cd ../..

    echo "✅ $service built successfully"
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 All images built successfully!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Images in Minikube:"
docker images | grep lanchonete | awk '{print "  - " $1 ":" $2 " (" $7 " " $8 ")"}'
