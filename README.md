# Sistema de Autoatendimento para Lanchonete - Fase 2

Este é um projeto para o Tech Challenge da pós-graduação SOAT - **Fase 2: Kubernetes**.

**Aluno:** Anderson Fér - rm363691
**Vídeo de demonstração:** https://youtu.be/XnkhaoebPZI

## 🚀 CI/CD Status
[![Pipeline 1 - Base](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-base.yml/badge.svg)](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-base.yml)
[![Pipeline 2 - K8s](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-k8s.yml/badge.svg)](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-k8s.yml)
[![Pipeline 3 - Auth](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-auth.yml/badge.svg)](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-auth.yml)
[![Pipeline 4 - App](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-app.yml/badge.svg)](https://github.com/anderson-faria-fiap/lanchonete-app/actions/workflows/pipeline-app.yml)

## Descrição do Projeto

Sistema de autoatendimento para lanchonete implementado com arquitetura de microserviços rodando em cluster Kubernetes. O sistema permite pedidos sem interação com atendentes, processamento de pagamentos via mock do Mercado Pago e gerenciamento completo de pedidos na cozinha, com escalabilidade automática baseada em demanda.

## Arquitetura do Sistema

### Visão Geral
Sistema distribuído em microserviços rodando em cluster Kubernetes (Minikube) com escalabilidade automática.

### Estrutura da Arquitetura

#### Arquitetura Kubernetes
<img src="diagramas/arquitetura-k8s.svg" alt="Fluxo da Cozinha" width="100%"/>

#### Clean Architecture 
```
lanchonete-app/
├── autoatendimento/                 # Microserviço principal
│   ├── src/main/java/
│   │   ├── dominio/                 # Camada de Domínio (Clean Architecture)
│   │   │   ├── entidades/           # Entidades de negócio
│   │   │   ├── valueobjects/        # CPF, Email, Preco
│   │   │   └── enums/               # StatusPedido, Categoria
│   │   ├── aplicacao/               # Camada de Aplicação
│   │   │   ├── casosdeuso/          # Use Cases por contexto
│   │   │   └── gateways/            # Interfaces para gateways
│   │   ├── adaptadores/             # Camada de Adaptadores
│   │   │   ├── rest/                # Controllers e Services
│   │   │   └── persistencia/        # Implementação de gateways
│   │   └── infra/                   # Camada de Infraestrutura
│   └── Dockerfile
├── pagamento/                       # Mock Mercado Pago
├── k8s/                            # Manifests Kubernetes
│   ├── deployments/                 # Deployments e StatefulSets
│   ├── services/                    # Services (NodePort, ClusterIP)
│   ├── configmaps/                  # Configurações não sensíveis
│   ├── secrets/                     # Credenciais e dados sensíveis
│   ├── storage/                     # PersistentVolumes e PVCs
│   └── hpa/                         # HorizontalPodAutoscaler
├── aplicar_manifests.sh             # Script de deploy automatizado
├── validar_deploy_k8s.sh           # Script de validação
├── teste-carga-hpa.sh              # Script de teste de escalabilidade
└── limpar_k8s.sh                   # Script de limpeza
```

#### Componentes Kubernetes

**APLICAÇÕES:**
- autoatendimento-deployment (2-4 pods) → lanchonete-app-autoatendimento:latest
- pagamento-deployment (2-4 pods) → lanchonete-app-pagamento:latest

**SERVIÇOS DE REDE:**
- autoatendimento-service (NodePort 30080) → autoatendimento pods
- pagamento-service (NodePort 30081) → pagamento pods
- mysql-service (ClusterIP 3306) → mysql pod

**BANCO DE DADOS:**
- mysql-statefulset (1 pod) + PersistentVolume 10Gi

**CONFIGURAÇÕES:**
- ConfigMaps: autoatendimento-config, pagamento-config
- Secrets: mysql-secret (credenciais do banco)

**ESCALABILIDADE:**
- HPA autoatendimento: 2-4 pods (CPU target 60%)
- HPA pagamento: 2-4 pods (Memory target 90%)

**ACESSO EXTERNO:**
- Autoatendimento: http://minikube-ip:30080
- Pagamento: http://minikube-ip:30081

## Tecnologias Utilizadas

- **Backend:** Java 17, Spring Boot 3.4.4, MySQL 8.0
- **Infraestrutura:** Kubernetes (Minikube), Docker, HPA
- **Arquitetura:** Clean Architecture, DDD, Microserviços

## Fluxos de Negócio
O sistema segue um fluxo dividido em 3 etapas principais:

### 1. Cliente e Checkout
<img src="diagramas/fluxo-cliente.svg" alt="Fluxo do Cliente" width="100%"/>

### 2. Processamento de Pagamento
<img src="diagramas/fluxo-pagamento.svg" alt="Fluxo de Pagamento" width="100%"/>

### 3. Operações da Cozinha
<img src="diagramas/fluxo-cozinha.svg" alt="Fluxo da Cozinha" width="100%"/>

## Como Executar

### Pré-requisitos
- **Minikube** instalado e funcionando
- **kubectl** configurado
- **Docker** (para build das imagens)
- **Git**

### 1. Setup do Ambiente

**Iniciar Minikube:**
```bash
minikube start
minikube addons enable metrics-server
```

**Clonar o repositório:**
```bash
git clone https://github.com/andersonfer/lanchonete-app.git
cd lanchonete-app
```

### 2. Build das Imagens

```bash
# Build das aplicações
docker-compose build

# Carregar imagens no Minikube
minikube image load lanchonete-app-autoatendimento:latest
minikube image load lanchonete-app-pagamento:latest
```

### 3. Deploy no Kubernetes

```bash
# Exportar as variáveis de ambiente
cp .env.example .env
export $(cat .env | grep -v '^#' | xargs)

# Deploy completo (ordem automatizada)
chmod +x aplicar_manifests.sh
./aplicar_manifests.sh
```

**Ou deploy manual por etapas:**
```bash
# Exportar as variáveis de ambiente
cp .env.example .env
export $(cat .env | grep -v '^#' | xargs)

# 1. Configurações
bash k8s/secrets/create-secrets.sh
kubectl apply -f k8s/configmaps/

# 2. Storage e MySQL
kubectl apply -f k8s/storage/
kubectl apply -f k8s/deployments/mysql-statefulset.yaml
kubectl apply -f k8s/services/mysql-services.yaml

# 3. Aplicações
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/app-services.yaml

# 4. Escalabilidade
kubectl apply -f k8s/hpa/
```

### 4. Verificar Deploy

```bash
# Status geral
kubectl get pods,services,hpa

# Validação completa
chmod +x validar_deploy_k8s.sh
./validar_deploy_k8s.sh
```

### 5. Acessar Aplicações

```bash
echo "Autoatendimento: http://$(minikube ip):30080/swagger-ui.html"
echo "Pagamento: http://$(minikube ip):30081/swagger-ui.html"
```

### 6. Limpeza (Opcional)

```bash
# Remover todos os recursos
chmod +x limpar_k8s.sh
./limpar_k8s.sh
```

## Testando o Sistema

### Teste Automatizado Completo

```bash
# Executar teste completo de todos os endpoints
chmod +x fluxo_completo.sh
./fluxo_completo.sh
```

### APIs Disponíveis e Roteiro de Testes

#### **Produtos**
- `GET /produtos/categoria/{categoria}` - Buscar produtos por categoria
    - Categorias: `LANCHE`, `BEBIDA`, `ACOMPANHAMENTO`, `SOBREMESA`

**1.1 Listar lanches:**
```bash
curl "http://$(minikube ip):30080/produtos/categoria/LANCHE"
```

---

**1.2 Listar bebidas:**
```bash
curl "http://$(minikube ip):30080/produtos/categoria/BEBIDA"
```

---

**1.3 Listar acompanhamentos:**
```bash
curl "http://$(minikube ip):30080/produtos/categoria/ACOMPANHAMENTO"
```

---

**1.4 Listar sobremesas:**
```bash
curl "http://$(minikube ip):30080/produtos/categoria/SOBREMESA"
```

#### **Pedidos**
- `POST /pedidos/checkout` - Realizar checkout de pedido

**2.1 Checkout do Pedido (captura o ID do pagamento):**
```bash
PEDIDO_RESPONSE=$(curl -s -X POST "http://$(minikube ip):30080/pedidos/checkout" \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": null,
    "itens": [
      {"produtoId": 1, "quantidade": 2},
      {"produtoId": 2, "quantidade": 1}
    ]
  }')

PEDIDO_ID=$(echo "$PEDIDO_RESPONSE" | jq -r '.id')

echo "Pedido criado com ID: $PEDIDO_ID"
```

#### **Pagamentos**
- `POST /pagamentos` - Processar pagamento (Mock Mercado Pago) *(Serviço Pagamento)*

**2.2 Processar Pagamento (usando ID capturado):**
```bash
curl -X POST "http://$(minikube ip):30081/pagamentos" \
  -H "Content-Type: application/json" \
  -d "{\"pedidoId\": \"$PEDIDO_ID\", \"valor\": 35.80}"
```

#### **Status de Pagamento**
- `GET /pedidos/{id}/pagamento/status` - Consultar status de pagamento

**2.3 Aguardar Webhook e Verificar Status:**
```bash
curl "http://$(minikube ip):30080/pedidos/$PEDIDO_ID/pagamento/status"
```
*Resposta esperada: `"APROVADO"` ou `"REJEITADO"`*

#### **Cozinha**
- `GET /pedidos/cozinha` - Listar pedidos da cozinha (ordenados por prioridade)

**3.1 Listar Pedidos da Cozinha (inicial):**
```bash
curl "http://$(minikube ip):30080/pedidos/cozinha"
```

- `PUT /pedidos/cozinha/{id}/status` - Atualizar status de pedidos
    - Status: `RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `FINALIZADO`

**3.2 Atualizar Status: RECEBIDO → EM_PREPARACAO:**
```bash
curl -X PUT "http://$(minikube ip):30080/pedidos/cozinha/$PEDIDO_ID/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "EM_PREPARACAO"}'
```

---

**3.3 Atualizar Status: EM_PREPARACAO → PRONTO:**
```bash
curl -X PUT "http://$(minikube ip):30080/pedidos/cozinha/$PEDIDO_ID/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "PRONTO"}'
```

---

**3.4 Atualizar Status: PRONTO → FINALIZADO:**
```bash
curl -X PUT "http://$(minikube ip):30080/pedidos/cozinha/$PEDIDO_ID/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "FINALIZADO"}'
```

#### **Gerenciamento de Pedidos**
- `GET /pedidos` - Listar todos os pedidos

**3.5 Verificar que removeu da cozinha:**
```bash
curl "http://$(minikube ip):30080/pedidos/cozinha"
```

---

**3.6 Verificar lista geral de pedidos:**
```bash
curl "http://$(minikube ip):30080/pedidos"
```

#### **CRUD de Produtos**
- `POST /produtos` - Criar produto

**4.1 Criar produto:**
```bash
PRODUTO_RESPONSE=$(curl -s -X POST "http://$(minikube ip):30080/produtos" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Produto Teste", 
    "descricao": "Produto para teste", 
    "preco": 15.50, 
    "categoria": "LANCHE"
  }')

PRODUTO_ID=$(echo $PRODUTO_RESPONSE | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)
echo "Produto criado com ID: $PRODUTO_ID"
```

- `PUT /produtos/{id}` - Editar produto

**4.2 Editar produto:**
```bash
curl -X PUT "http://$(minikube ip):30080/produtos/$PRODUTO_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Produto Teste Editado", 
    "descricao": "Produto editado", 
    "preco": 17.90, 
    "categoria": "LANCHE"
  }'
```

- `DELETE /produtos/{id}` - Remover produto

**4.3 Deletar produto:**
```bash
curl -X DELETE "http://$(minikube ip):30080/produtos/$PRODUTO_ID"
```

### **Clientes**
- `POST /clientes` - Cadastrar cliente

**5.1 Cadastrar novo cliente:**
```bash
CLIENTE_RESPONSE=$(curl -s -X POST "http://$(minikube ip):30080/clientes" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Cliente Teste",
    "cpf": "98765432100", 
    "email": "teste@lanchonete.com"
  }')

echo "Cliente cadastrado: $CLIENTE_RESPONSE"
```

- `GET /clientes/cpf/{cpf}` - Buscar cliente por CPF

**5.2 Buscar cliente por CPF:**
```bash
curl "http://$(minikube ip):30080/clientes/cpf/98765432100"
```

### 📖 **Documentação**

**6. Acessar documentação swagger:**
```bash
echo "Autoatendimento: http://$(minikube ip):30080/swagger-ui.html"
echo "Pagamento: http://$(minikube ip):30081/swagger-ui.html"
```

### 📋 **Resumo dos Testes**

Após executar todos os passos acima, você terá testado:

✅ **Produtos**: Listagem por categoria, CRUD completo  
✅ **Clientes**: Cadastro e busca por CPF  
✅ **Pedidos**: Checkout com cliente identificado  
✅ **Pagamentos**: Processamento via Mock Mercado Pago  
✅ **Cozinha**: Fluxo completo de status dos pedidos  
✅ **Webhooks**: Comunicação entre serviços  
✅ **Documentação**: Swagger UI funcionando


## Escalabilidade (HPA)

### Configuração Atual

O sistema está configurado com **Horizontal Pod Autoscaler** para ajustar automaticamente o número de pods baseado na demanda:

**Autoatendimento:**
- **Mínimo:** 2 pods
- **Máximo:** 4 pods
- **Métrica:** CPU 60%
- **Comportamento:** Scale up em 1 minuto, scale down em 3 minutos

### Testar Escalabilidade

#### **Executar Teste de Carga Automatizado**

```bash
# Teste de 5 minutos com escalabilidade automática
chmod +x teste-carga-hpa.sh
./teste-carga-hpa.sh
```

#### **Monitoramento em Tempo Real**

**Em terminal separado, acompanhe a escalabilidade:**
```bash
# Monitorar HPA (atualização contínua)
kubectl get hpa -w

# Monitorar pods (criação/destruição)
kubectl get pods -l app=autoatendimento -w

# Monitorar métricas de CPU
watch kubectl top pods -l app=autoatendimento
```

### Comportamento Esperado

**Durante Carga Alta:**
- CPU aumenta de ~6% para 60%+
- HPA escala de 2 para 3-4 pods em ~1-2 minutos
- Requisições distribuídas entre pods

**Após Carga:**
- CPU retorna para ~6%
- HPA aguarda 3 minutos de estabilização
- Scale down gradual para 2 pods

**Tempos Típicos:**
- **Scale Up:** 1-2 minutos
- **Scale Down:** 3-5 minutos
- **Estabilização:** 5-10 minutos total

## Limpeza

### Remover Todos os Recursos

```bash
# Remover todos os recursos do projeto
chmod +x limpar_k8s.sh
./limpar_k8s.sh
```

---

**Tech Challenge SOAT - Fase 2**  
**Sistema de Autoatendimento com Kubernetes e Escalabilidade Automática**
