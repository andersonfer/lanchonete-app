# Sistema de Autoatendimento para Lanchonete

Este é um projeto para o Tech Challenge da pós-graduação SOAT - fase 2.

**Aluno:** Anderson Fér - rm363691  
**Vídeo de demonstração:** https://youtu.be/lOOEKc5jY6I

## Descrição do Projeto

Sistema de autoatendimento para uma lanchonete de bairro, permitindo que os clientes façam pedidos sem interagir com um atendente. O sistema integra com um mock do Mercado Pago para processamento de pagamentos e possui um fluxo completo de gerenciamento de pedidos na cozinha.

## Arquitetura do Sistema

O projeto é composto por **dois microserviços** implementados em Java 17 com Spring Boot:

### 🍔 **Autoatendimento** (Porta 8080)
- Sistema principal de pedidos
- Clean Architecture + DDD
- Integração com webhook de pagamento
- APIs para cozinha
- Banco MySQL em produção

### 💳 **Pagamento** (Porta 8081)
- Mock do Mercado Pago
- Simulação de processamento de pagamento
- Webhook automático para autoatendimento
- Comportamento aleatório (80% aprovação)

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.4.4**
- **Spring Data JDBC**
- **MySQL 8.0** (produção)
- **H2 Database** (desenvolvimento)
- **Docker & Docker Compose**
- **SpringDoc OpenAPI (Swagger)**
- **WebClient** (comunicação entre serviços)

## 🚀 Como Executar

### Pré-requisitos
- Docker
- Docker Compose
- Git

### Executando com Docker Compose

1. **Clone o repositório:**
```bash
git clone https://github.com/andersonfer/lanchonete-app.git
cd lanchonete-app
```

2. **Execute o Docker Compose:**
```bash
docker-compose up -d
```

3. **Aguarde os serviços iniciarem** (≈ 30 segundos)

4. **Acesse a documentação:**
   - **Autoatendimento:** http://localhost:8080/swagger-ui.html
   - **Pagamento:** http://localhost:8081/swagger-ui.html

### Serviços Disponíveis

| Serviço | Porta | URL Base | Descrição |
|---------|-------|----------|-----------|
| Autoatendimento | 8080 | http://localhost:8080 | Sistema principal |
| Pagamento | 8081 | http://localhost:8081 | Mock Mercado Pago |
| MySQL | 3306 | jdbc:mysql://localhost:3306/lanchonete | Banco de dados |

## 🧪 Testando o Fluxo Completo

### Cenário: Pedido Completo até Finalização

Vamos simular um fluxo completo desde o pedido até a finalização na cozinha:

### 1. 📦 Consultar Produtos Disponíveis

```bash
# Listar lanches
curl -X GET "http://localhost:8080/produtos/categoria/LANCHE"

# Listar bebidas
curl -X GET "http://localhost:8080/produtos/categoria/BEBIDA"

# Listar acompanhamentos
curl -X GET "http://localhost:8080/produtos/categoria/ACOMPANHAMENTO"

# Listar sobremesas
curl -X GET "http://localhost:8080/produtos/categoria/SOBREMESA"
```

**Resposta exemplo:**
```json
[
  {
    "id": 1,
    "nome": "X-Burger",
    "descricao": "Hambúrguer com queijo, alface e tomate",
    "preco": 18.90,
    "categoria": "LANCHE"
  }
]
```

### 2. 🛒 Realizar Checkout do Pedido

```bash
curl -X POST "http://localhost:8080/pedidos/checkout" \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": null,
    "itens": [
      {
        "produtoId": 1,
        "quantidade": 2
      }
    ]
  }'
```

**Resposta:**
```json
{
  "id": 1,
  "numeroPedido": "PED000001",
  "cpfCliente": null,
  "nomeCliente": null,
  "itens": [
    {
      "id": 1,
      "produtoId": 1,
      "nomeProduto": "X-Burger",
      "quantidade": 2,
      "valorUnitario": 18.90,
      "valorTotal": 37.80
    }
  ],
  "status": "RECEBIDO",
  "statusPagamento": "PENDENTE",
  "dataCriacao": "2025-07-16T12:00:00",
  "valorTotal": 37.80
}
```

### 3. 💳 Processar Pagamento

```bash
curl -X POST "http://localhost:8081/pagamentos" \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": "1",
    "valor": 37.80
  }'
```

**Resposta:**
```json
{
  "pedidoId": "1",
  "status": "PENDENTE"
}
```

⏱️ **Aguarde 3-5 segundos** para o processamento automático.

### 4. ✅ Verificar Status do Pagamento

```bash
curl -X GET "http://localhost:8080/pedidos/1/pagamento/status"
```

**Resposta (se aprovado):**
```json
{
  "pedidoId": 1,
  "statusPagamento": "APROVADO",
  "mensagem": "Pagamento aprovado com sucesso"
}
```

### 5. 👨‍🍳 Cozinha - Visualizar Pedidos

```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```

**Resposta:**
```json
[
  {
    "id": 1,
    "numeroPedido": "PED000001",
    "status": "RECEBIDO",
    "statusPagamento": "APROVADO",
    "dataCriacao": "2025-07-16T12:00:00",
    "valorTotal": 37.80,
    "itens": [
      {
        "nomeProduto": "X-Burger",
        "quantidade": 2
      }
    ]
  }
]
```

### 6. 🔄 Atualizar Status do Pedido (Cozinha)

#### Iniciar Preparação
```bash
curl -X PUT "http://localhost:8080/pedidos/cozinha/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "EM_PREPARACAO"
  }'
```

**Verificar na lista da cozinha:**
```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```

**Resposta (status mudou para EM_PREPARACAO):**
```json
[
  {
    "id": 1,
    "numeroPedido": "PED000001",
    "status": "EM_PREPARACAO",
    "statusPagamento": "APROVADO",
    "valorTotal": 37.80,
    "itens": [
      {
        "nomeProduto": "X-Burger",
        "quantidade": 2
      }
    ]
  }
]
```

#### Finalizar Preparação
```bash
curl -X PUT "http://localhost:8080/pedidos/cozinha/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PRONTO"
  }'
```

**Verificar na lista da cozinha:**
```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```

**Resposta (status mudou para PRONTO - prioridade máxima):**
```json
[
  {
    "id": 1,
    "numeroPedido": "PED000001",
    "status": "PRONTO",
    "statusPagamento": "APROVADO",
    "valorTotal": 37.80,
    "itens": [
      {
        "nomeProduto": "X-Burger",
        "quantidade": 2
      }
    ]
  }
]
```

#### Entregar Pedido
```bash
curl -X PUT "http://localhost:8080/pedidos/cozinha/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "FINALIZADO"
  }'
```

### 7. 📋 Verificar Lista da Cozinha (Pedido Finalizado)

```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```

**Resposta:**
```json
[]
```

✅ **Pedidos com status `FINALIZADO` não aparecem na lista da cozinha!**

**Observação:** O pedido foi finalizado com sucesso e removido da lista da cozinha. Apenas pedidos com status `RECEBIDO`, `EM_PREPARACAO` ou `PRONTO` aparecem na lista.

## 🔄 Fluxo de Status do Pedido

```
RECEBIDO → EM_PREPARACAO → PRONTO → FINALIZADO
```

### Estados do Pedido:
- **RECEBIDO**: Pedido criado, aguardando pagamento
- **EM_PREPARACAO**: Cozinha iniciou o preparo
- **PRONTO**: Pedido pronto para retirada
- **FINALIZADO**: Pedido entregue (não aparece na cozinha)

### Estados do Pagamento:
- **PENDENTE**: Aguardando processamento
- **APROVADO**: Pagamento aprovado
- **REJEITADO**: Pagamento rejeitado

## 📊 Regras de Ordenação da Cozinha

A lista de pedidos da cozinha é ordenada por:
1. **Status** (prioridade): PRONTO > EM_PREPARACAO > RECEBIDO
2. **Data de criação**: Mais antigos primeiro

### 📋 Comportamento da Lista

- **RECEBIDO**: Pedidos que aguardam início do preparo
- **EM_PREPARACAO**: Pedidos sendo preparados pela cozinha
- **PRONTO**: Pedidos prontos para retirada (aparecem primeiro na lista)
- **FINALIZADO**: Pedidos entregues (NÃO aparecem na lista)

### 🔄 Verificação de Status

Para cada mudança de status, consulte a lista da cozinha para verificar:
- A alteração foi aplicada
- A nova posição na ordenação
- Se o pedido ainda aparece na lista (não aparece se FINALIZADO)

## 🛠️ APIs Principais

### Autoatendimento (8080)
- `GET /produtos/categoria/{categoria}` - Listar produtos
- `POST /pedidos/checkout` - Realizar pedido
- `GET /pedidos/{id}/pagamento/status` - Status do pagamento
- `GET /pedidos/cozinha` - Lista para cozinha
- `PUT /pedidos/cozinha/{id}/status` - Atualizar status

### Pagamento (8081)
- `POST /pagamentos` - Processar pagamento
- `POST /webhook/pagamento` - Webhook (automático)

## 🔧 Desenvolvimento Local

### Executando apenas o Autoatendimento (H2)
```bash
cd autoatendimento/
mvn spring-boot:run
```

### Executando Testes
```bash
cd autoatendimento/
mvn test

cd pagamento/
mvn test
```

## 🐳 Docker Compose

O arquivo `docker-compose.yml` orquestra:
- MySQL 8.0 com dados iniciais
- Autoatendimento (com MySQL)
- Pagamento (mock)
- Rede isolada para comunicação

### Comandos Úteis:
```bash
# Subir serviços
docker-compose up -d

# Ver logs
docker-compose logs -f autoatendimento
docker-compose logs -f pagamento

# Parar serviços
docker-compose down

# Rebuild
docker-compose up --build -d
```

## 🎯 Webhook Automático

O serviço de **Pagamento** simula o comportamento do Mercado Pago:

1. **Processa pagamento** (3-5 segundos)
2. **Decide resultado** (80% aprovação, 20% rejeição)
3. **Chama webhook** automaticamente
4. **Autoatendimento** atualiza status do pedido

### Simulação Realística:
- ✅ **80% dos pagamentos** são aprovados
- ❌ **20% dos pagamentos** são rejeitados
- ⏱️ **Delay de 3-5 segundos** para simular processamento

## 📁 Estrutura do Projeto

```
lanchonete-app/
├── autoatendimento/          # Microserviço principal
│   ├── src/main/java/        # Código fonte
│   ├── src/test/java/        # Testes
│   └── Dockerfile            # Container do autoatendimento
├── pagamento/                # Mock Mercado Pago
│   ├── src/main/java/        # Código fonte
│   ├── src/test/java/        # Testes
│   └── Dockerfile            # Container do pagamento
├── docker-compose.yml        # Orquestração dos serviços
└── README.md                 # Este arquivo
```

## 🎬 Exemplo de Uso Completo

### 1. Subir aplicação
```bash
docker-compose up -d
```

### 2. Fazer pedido
```bash
curl -X POST "http://localhost:8080/pedidos/checkout" \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente": null, "itens": [{"produtoId": 1, "quantidade": 1}]}'
```

### 3. Processar pagamento
```bash
curl -X POST "http://localhost:8081/pagamentos" \
  -H "Content-Type: application/json" \
  -d '{"pedidoId": "1", "valor": 18.90}'
```

### 4. Aguardar webhook automático (3-5s)
```bash
sleep 5
```

### 5. Verificar aprovação
```bash
curl -X GET "http://localhost:8080/pedidos/1/pagamento/status"
```

### 6. Cozinha - ver pedidos
```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```

### 7. Atualizar para EM_PREPARACAO
```bash
curl -X PUT "http://localhost:8080/pedidos/cozinha/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "EM_PREPARACAO"}'
```

### 7.1. Verificar mudança de status na cozinha
```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```
*Resposta: pedido com status "EM_PREPARACAO"*

### 8. Atualizar para PRONTO
```bash
curl -X PUT "http://localhost:8080/pedidos/cozinha/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "PRONTO"}'
```

### 8.1. Verificar mudança de status na cozinha
```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```
*Resposta: pedido com status "PRONTO" (prioridade máxima)*

### 9. Finalizar pedido
```bash
curl -X PUT "http://localhost:8080/pedidos/cozinha/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "FINALIZADO"}'
```

### 10. Verificar que não aparece mais na cozinha
```bash
curl -X GET "http://localhost:8080/pedidos/cozinha"
```
*Resposta: [] (pedido finalizado não aparece mais)*

## 🏗️ Arquitetura Técnica

### Autoatendimento
- **Clean Architecture** com DDD
- **Camadas**: Domínio → Aplicação → Adaptadores → Infraestrutura
- **Padrões**: Repository, Use Cases, DTOs
- **Banco**: MySQL (produção), H2 (desenvolvimento)

### Pagamento
- **Arquitetura Simples** para mock
- **Simulação realística** do Mercado Pago
- **Webhook automático** com WebClient
- **Comportamento probabilístico**

## 🚨 Troubleshooting

### Problema: Containers não sobem
```bash
# Verificar ports em uso
docker-compose down
docker-compose up -d
```

### Problema: Webhook não funciona
```bash
# Verificar logs
docker-compose logs pagamento
docker-compose logs autoatendimento
```

### Problema: Base de dados
```bash
# Recrear volumes
docker-compose down -v
docker-compose up -d
```

---

**Tech Challenge SOAT - Fase 2**  
**Sistema de Autoatendimento com Integração de Pagamento**