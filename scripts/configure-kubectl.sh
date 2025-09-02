#!/bin/bash

set -e

echo "⚙️  CONFIGURANDO KUBECTL PARA EKS"
echo "================================="

# Verificar se AWS CLI está configurado
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ Credenciais AWS não configuradas!"
    echo "💡 Configure as credenciais primeiro"
    exit 1
fi

# Verificar se kubectl está instalado
if ! command -v kubectl &> /dev/null; then
    echo "📦 kubectl não encontrado. Instalando..."
    
    # Baixar kubectl
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    
    # Tornar executável
    chmod +x kubectl
    
    # Mover para PATH
    sudo mv kubectl /usr/local/bin/
    
    echo "✅ kubectl instalado com sucesso"
fi

KUBECTL_VERSION=$(kubectl version --client --short 2>/dev/null | cut -d' ' -f3)
echo "🔧 kubectl versão: $KUBECTL_VERSION"

# Obter nome do cluster do Terraform
if [[ -f "terraform/kubernetes/terraform.tfstate" ]]; then
    CLUSTER_NAME=$(cd terraform/kubernetes && terraform output -raw cluster_name 2>/dev/null || echo "")
else
    CLUSTER_NAME="lanchonete-cluster"
fi

if [[ -z "$CLUSTER_NAME" ]]; then
    echo "❌ Nome do cluster não encontrado. Usando padrão: lanchonete-cluster"
    CLUSTER_NAME="lanchonete-cluster"
fi

echo "☸️  Configurando kubectl para cluster: $CLUSTER_NAME"

# Atualizar configuração do kubectl
aws eks update-kubeconfig --region us-east-1 --name "$CLUSTER_NAME"

if [[ $? -eq 0 ]]; then
    echo "✅ kubectl configurado com sucesso!"
else
    echo "❌ Erro ao configurar kubectl"
    exit 1
fi

# Testar conexão
echo "🧪 Testando conectividade..."
kubectl cluster-info --request-timeout=30s

if [[ $? -eq 0 ]]; then
    echo ""
    echo "✅ CONFIGURAÇÃO CONCLUÍDA!"
    echo "=========================="
    echo "🎯 Próximos comandos úteis:"
    echo "   kubectl get nodes"
    echo "   kubectl get pods --all-namespaces"
    echo "   kubectl get services --all-namespaces"
else
    echo "❌ Não foi possível conectar ao cluster"
    echo "💡 Verifique se o cluster está rodando e acessível"
    exit 1
fi