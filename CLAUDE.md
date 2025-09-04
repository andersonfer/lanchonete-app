# CLAUDE.md - Tech Challenge Fase 3

## 🎯 ESTRATÉGIA DE IMPLEMENTAÇÃO INCREMENTAL

### Abordagem de Deploy Progressivo
Este projeto será implementado em **8 etapas incrementais** com validação obrigatória entre cada fase:

1. **Deploy Progressivo**: Cada etapa gera apenas os artefatos necessários
2. **Validação Obrigatória**: Scripts de teste específicos para cada componente
3. **Critérios de Sucesso**: Checklist claro antes de prosseguir
4. **Rollback Granular**: Possibilidade de voltar atrás em qualquer ponto

### Como Usar Este Documento
- **Para gerar uma etapa**: Solicite "Gerar artefatos da ETAPA X"
- **Para validar**: Execute os comandos de deploy fornecidos
- **Para marcar como concluída**: Diga "ETAPA X concluída com sucesso"
- **Para atualizar**: Claude Code atualizará automaticamente o status

### INSTRUÇÕES ESPECIAIS PARA CLAUDE CODE
```
PADRÕES DE CODIFICAÇÃO:
- Todas classes, métodos, comentários e testes em PORTUGUÊS
- Evitar comentários desnecessários (changelog vai no git)
- Manter apenas documentação técnica essencial (contratos, arquitetura)
- Seguir padrão do pacote br.com.lanchonete.autoatendimento
- NUNCA usar System.out.println - sempre usar logs estruturados
- Exemplo de log: logger.info("Produto criado com ID: {}", produto.getId())

GESTÃO DE COMMITS:
- Ao finalizar grupo de artefatos relacionados, sugerir mensagem de commit
- Mensagem deve ser UMA FRASE concisa descrevendo o que foi implementado
- Agrupar artefatos logicamente (ex: todos TFs de uma etapa juntos)
- Usuário copiará/editará a mensagem conforme necessário
- Quando usuário confirmar "commit feito", atualizar log do CLAUDE.md

AUTOMAÇÃO DE STATUS:
- Monitore "ETAPA X concluída" para atualizar [ ] TODO → [ ] DONE
- Mantenha log das mudanças no final do documento
- Preserve estrutura e formatação original
```

### Status de Implementação
```
📅 CRONOGRAMA DE ETAPAS

🏗️ ETAPA 1: RDS + Lambda Connectivity Test   [ ] TODO / [ ] DOING / [X] DONE
⚡ ETAPA 2: Lambda de Autenticação Completa   [ ] TODO / [ ] DOING / [X] DONE  
🌐 ETAPA 3: API Gateway + JWT Authorizer     [ ] TODO / [ ] DOING / [X] DONE
☸️ ETAPA 4: EKS Cluster                      [ ] TODO / [ ] DOING / [X] DONE
📦 ETAPA 5: Aplicações Migradas              [ ] TODO / [ ] DOING / [X] DONE
☸️ ETAPA 6: Deploy Kubernetes                [ ] TODO / [ ] DOING / [ ] DONE
🔗 ETAPA 7: Integração API Gateway ↔ EKS    [ ] TODO / [ ] DOING / [ ] DONE
🚀 ETAPA 8: CI/CD + Finalização             [ ] TODO / [ ] DOING / [ ] DONE
```

**COMANDO PARA ATUALIZAÇÃO AUTOMÁTICA**:
- "ETAPA 1 concluída" → Claude Code marca ETAPA 1 como DONE
- "ETAPA 2 finalizada com sucesso" → Claude Code marca ETAPA 2 como DONE
- "Terminei a ETAPA 3" → Claude Code marca ETAPA 3 como DONE

---

## 📋 PLANO DETALHADO DAS ETAPAS

### 🏗️ ETAPA 1: RDS + Lambda Connectivity Test
**Duração**: 4-5 horas | **Status**: [ ] TODO

**Objetivo**: Infraestrutura RDS MySQL funcionando + Lambda conseguindo conectar

**Estratégia**: Resolver problemas de conectividade VPC/Security Groups primeiro com Lambda simples

**Artefatos**:
- terraform/shared/ (variables, data sources, academy config)  
- terraform/database/ (RDS, security groups, migrations)
- lambda-connectivity-test/ (Lambda simples só para testar conexão)
- scripts/setup-aws-academy.sh
- scripts/validate-database-connectivity.sh

**Deploy**: 
```bash
./scripts/setup-aws-academy.sh
cd terraform/database && terraform apply -var-file="../shared/academy.tfvars"
./scripts/validate-database-connectivity.sh
```

**Critérios de Sucesso**: 
- RDS rodando privadamente (não público)
- Lambda consegue conectar via VPC
- Schema criado + seeds inseridos
- Logs da Lambda confirmando conectividade
- Security Groups permitindo comunicação Lambda ↔ RDS

**Sinalização para Claude Code**: "ETAPA 1 concluída"

---

### ⚡ ETAPA 2: Lambda de Autenticação Completa
**Duração**: 3-4 horas | **Status**: [ ] TODO

**Objetivo**: Lambda de autenticação (CPF + anônimo) funcionando completamente

**Estratégia**: Reutilizar configurações de VPC/SG da etapa 1, implementar lógica de auth

**Artefatos**:
- lambda-auth/ completo (AuthHandler, services, models, testes)
- Reutilização das configurações de rede da ETAPA 1
- pom.xml + template.yml
- scripts/test-lambda-auth.sh
- scripts/validate-lambda.sh

**Deploy**:
```bash
cd lambda-auth && mvn clean package
sam build && sam deploy --guided  
./scripts/test-lambda-auth.sh
./scripts/validate-lambda.sh
```

**Critérios de Sucesso**: 
- Lambda deployed com lógica completa
- Auth CPF funcionando (busca no RDS)
- Auth anônimo funcionando (sessão temporária)
- JWT gerado corretamente
- Conectividade RDS já resolvida da etapa anterior

**Sinalização para Claude Code**: "ETAPA 2 concluída"

---

### 🌐 ETAPA 3: API Gateway + JWT Authorizer
**Duração**: 3-4 horas | **Status**: [ ] TODO  

**Objetivo**: API Gateway com autenticação funcionando

**Artefatos**:
- terraform/lambda/ (API Gateway, routes, JWT authorizer)
- JwtAuthorizerHandler.java
- scripts/test-api-gateway.sh  
- scripts/validate-auth-flow.sh

**Deploy**:
```bash
cd terraform/lambda && terraform apply -var-file="../shared/academy.tfvars"
./scripts/test-api-gateway.sh
./scripts/validate-auth-flow.sh
```

**Critérios de Sucesso**: API Gateway operacional + JWT Authorizer + /auth público + routes protegidas

**Sinalização para Claude Code**: "ETAPA 3 concluída"

---

### ☸️ ETAPA 4: EKS Cluster
**Duração**: 2-3 horas | **Status**: [ ] TODO

**Objetivo**: Cluster EKS pronto para aplicações

**Artefatos**:
- terraform/kubernetes/ (EKS, VPC Link, ALB)
- scripts/configure-kubectl.sh
- scripts/test-eks-connectivity.sh
- scripts/validate-cluster.sh

**Deploy**:
```bash
cd terraform/kubernetes && terraform apply -var-file="../shared/academy.tfvars"
./scripts/configure-kubectl.sh
./scripts/test-eks-connectivity.sh
./scripts/validate-cluster.sh
```

**Critérios de Sucesso**: EKS ativo + nodes prontos + kubectl funcionando + VPC Link criado

**Sinalização para Claude Code**: "ETAPA 4 concluída"

---

### 📦 ETAPA 5: Aplicações Migradas
**Duração**: 3-4 horas | **Status**: [ ] TODO

**Objetivo**: Apps da Fase 2 adaptadas para API Gateway context injection

**Artefatos**:
- applications/ (autoatendimento + pagamento adaptados)
- ApiGatewayContextFilter.java
- application-kubernetes.yml adaptado
- scripts/build-images.sh
- scripts/validate-apps.sh

**Deploy**:
```bash
./scripts/build-images.sh
./scripts/push-to-ecr.sh  
./scripts/validate-apps.sh
```

**Critérios de Sucesso**: Código migrado + context filter + images buildadas + ECR push

**Sinalização para Claude Code**: "ETAPA 5 concluída"

---

### ☸️ ETAPA 6: Deploy Kubernetes  
**Duração**: 2-3 horas | **Status**: [ ] TODO

**Objetivo**: Aplicações rodando no EKS

**Artefatos**:
- k8s-manifests/ completo (deployments, services, HPA, ingress)
- scripts/deploy-k8s.sh
- scripts/validate-k8s-apps.sh

**Deploy**:
```bash
./scripts/deploy-k8s.sh
kubectl wait --for=condition=ready pod -l app=autoatendimento --timeout=300s
./scripts/validate-k8s-apps.sh
```

**Critérios de Sucesso**: Pods rodando + services OK + ALB configurado + HPA ativo

**Sinalização para Claude Code**: "ETAPA 6 concluída"

---

### 🔗 ETAPA 7: Integração API Gateway ↔ EKS
**Duração**: 2-3 horas | **Status**: [ ] TODO

**Objetivo**: API Gateway roteando para EKS com context injection

**Artefatos**:
- terraform/lambda/api-gateway-routes.tf (routes para EKS)
- scripts/test-end-to-end.sh
- scripts/test-context-injection.sh  
- scripts/validate-integration.sh

**Deploy**:
```bash
cd terraform/lambda && terraform apply -var-file="../shared/academy.tfvars"
./scripts/test-end-to-end.sh
./scripts/validate-integration.sh
```

**Critérios de Sucesso**: API Gateway → EKS funcionando + context injection + autenticação end-to-end

**Sinalização para Claude Code**: "ETAPA 7 concluída"

---

### 🚀 ETAPA 8: CI/CD + Finalização
**Duração**: 3-4 horas | **Status**: [ ] TODO

**Objetivo**: GitHub Actions + documentação + vídeo

**Artefatos**:
- .github/workflows/ completo
- docs/ completo  
- README.md final
- VIDEO.md
- scripts/test-complete-flow.sh

**Deploy**:
```bash
git add .github/workflows/ && git commit && git push
./scripts/test-complete-flow.sh
./scripts/prepare-video.sh
```

**Critérios de Sucesso**: Workflows funcionando + PRs fechados + teste end-to-end + ambiente para vídeo

**Sinalização para Claude Code**: "ETAPA 8 concluída"

---

## 📋 VISÃO GERAL DO PROJETO

### Contexto
- **Projeto**: Migração do sistema de lanchonete da Fase 2 para arquitetura cloud-native
- **Plataforma**: AWS Academy (usando LabRole)
- **Estrutura**: Monorepo com separação por contextos
- **Teste**: Via cURL (sem frontend)
- **Entrega**: Repositório privado + vídeo demonstrativo

### Objetivos Técnicos
1. **API Gateway** como ponto de entrada único
2. **Lambda de autenticação** (CPF + anônimo) sem Cognito
3. **EKS** para aplicações existentes
4. **RDS MySQL** gerenciado
5. **CI/CD** completo com GitHub Actions
6. **Terraform** para toda infraestrutura
7. **JWT** para autenticação stateless

## 🏗️ ARQUITETURA TÉCNICA

### Fluxo de Requisições
```
📱 Cliente (cURL) 
   ↓ POST /auth {cpf: "123"}
🌐 API Gateway 
   ↓ AWS_PROXY
⚡ Lambda Auth (Java)
   ↓ buscarPorCpf()
🗄️ RDS MySQL
   ↓ JWT gerado
🎫 Token retornado
   ↓ Authorization: Bearer jwt
🌐 API Gateway + JWT Authorizer
   ↓ Headers injetados (X-Cliente-ID, X-Auth-Type)
📡 VPC Link → ALB
   ↓ 
☸️ EKS Pods
   ↓
🗄️ RDS MySQL
```

### Componentes AWS
- **API Gateway**: Controle de acesso, roteamento, JWT authorizer
- **Lambda**: Autenticação (Java 17, Spring Boot)
- **EKS**: Cluster Kubernetes para aplicações
- **RDS**: MySQL 8.0 gerenciado
- **VPC Link**: Comunicação privada API Gateway → EKS
- **ALB**: Application Load Balancer interno
- **ECR**: Registry para imagens Docker

## 📂 ESTRUTURA DO MONOREPO

```
lanchonete-tech-challenge-fase3/
├── .github/workflows/              # CI/CD GitHub Actions
│   ├── deploy-lambda.yml          # Deploy Lambda + API Gateway
│   ├── deploy-infra-k8s.yml       # Deploy EKS + VPC Link
│   ├── deploy-infra-db.yml        # Deploy RDS
│   ├── deploy-applications.yml     # Deploy apps para EKS
│   └── cleanup.yml                # Limpeza de recursos
├── terraform/                     # Infraestrutura como código
│   ├── lambda/                    # API Gateway + Lambda
│   │   ├── main.tf               # Provider, data sources
│   │   ├── lambda.tf             # Lambda function
│   │   ├── api-gateway.tf        # API Gateway + routes
│   │   ├── authorizer.tf         # JWT Authorizer
│   │   └── variables.tf          # Variáveis
│   ├── database/                 # RDS MySQL
│   │   ├── main.tf               # Provider, data sources
│   │   ├── rds.tf                # RDS instance
│   │   ├── security-groups.tf    # Security groups
│   │   ├── variables.tf          # Variáveis
│   │   └── migrations/           # Scripts SQL
│   │       ├── 001_create_schema.sql
│   │       └── 002_seed_data.sql
│   ├── kubernetes/               # EKS + Networking
│   │   ├── main.tf               # Provider, data sources
│   │   ├── eks.tf                # EKS cluster + node groups
│   │   ├── vpc-link.tf           # VPC Link para API Gateway
│   │   ├── alb.tf                # Application Load Balancer
│   │   └── variables.tf          # Variáveis
│   └── shared/                   # Recursos compartilhados
│       ├── variables.tf          # Variáveis globais
│       └── academy.tfvars        # Valores para AWS Academy
├── lambda-auth/                  # Função de autenticação
│   ├── src/main/java/br/com/lanchonete/auth/
│   │   ├── AuthHandler.java      # Handler principal
│   │   ├── service/
│   │   │   ├── AuthenticationService.java  # Lógica de auth
│   │   │   ├── JwtService.java             # Geração/validação JWT
│   │   │   ├── ClienteService.java         # Consulta RDS
│   │   │   └── CpfValidator.java           # Validação CPF
│   │   ├── model/
│   │   │   ├── AuthRequest.java            # DTO request
│   │   │   ├── AuthResponse.java           # DTO response
│   │   │   ├── Cliente.java                # Model cliente
│   │   │   └── ClienteDto.java             # DTO cliente
│   │   └── util/
│   │       └── JwtAuthorizerHandler.java   # JWT Authorizer
│   ├── src/test/java/             # Testes unitários
│   ├── pom.xml                   # Dependências Maven
│   ├── template.yml              # SAM template (alternativo)
│   └── README.md                 # Doc específica
├── applications/                 # Apps migradas da Fase 2
│   ├── autoatendimento/          # API principal (Clean Architecture)
│   │   ├── src/main/java/br/com/lanchonete/autoatendimento/
│   │   │   ├── dominio/          # Entities, VOs, Enums
│   │   │   ├── aplicacao/        # Use Cases, Gateways
│   │   │   ├── adaptadores/      # Controllers, Repositories
│   │   │   └── infra/            # Configuration, Security
│   │   ├── src/main/resources/
│   │   │   ├── application.yml   # Config base
│   │   │   ├── application-kubernetes.yml  # Config EKS
│   │   │   ├── schema-mysql.sql  # Schema
│   │   │   └── data-mysql.sql    # Dados iniciais
│   │   ├── Dockerfile            # Container
│   │   └── pom.xml              # Dependências
│   └── pagamento/               # Mock Mercado Pago
│       ├── src/main/java/br/com/lanchonete/pagamento/
│       ├── Dockerfile
│       └── pom.xml
├── k8s-manifests/               # Kubernetes YAML
│   ├── applications/
│   │   ├── autoatendimento-deployment.yaml
│   │   ├── pagamento-deployment.yaml
│   │   └── services.yaml        # ClusterIP services
│   ├── configmaps/
│   │   ├── autoatendimento-configmap.yaml
│   │   └── pagamento-configmap.yaml
│   ├── secrets/
│   │   └── create-secrets.sh    # Script para secrets
│   ├── hpa/
│   │   ├── autoatendimento-hpa.yaml
│   │   └── pagamento-hpa.yaml
│   └── ingress/
│       └── alb-ingress.yaml     # ALB Ingress Controller
├── docs/                        # Documentação
│   ├── architecture/
│   │   ├── architecture-overview.md
│   │   ├── database-design.md
│   │   └── api-flows.md
│   ├── deployment/
│   │   ├── aws-academy-setup.md
│   │   └── troubleshooting.md
│   └── diagrams/               # Diagramas arquiteturais
├── scripts/                    # Scripts utilitários
│   ├── setup-aws-academy.sh   # Setup inicial AWS Academy
│   ├── deploy-all.sh          # Deploy completo ordenado
│   ├── test-complete-flow.sh  # Teste end-to-end via cURL
│   ├── test-endpoints.sh      # Teste individual de APIs
│   └── cleanup-all.sh         # Limpeza completa
├── .gitignore                 # Git ignore
├── README.md                  # Documentação principal
└── VIDEO.md                   # Link e descrição do vídeo
```

## 🔑 ESPECIFICAÇÃO DA LAMBDA DE AUTENTICAÇÃO

### AuthHandler.java
```java
// Características técnicas:
// - Implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
// - Runtime: java17
// - Handler: br.com.lanchonete.auth.AuthHandler::handleRequest
// - Memory: 512MB
// - Timeout: 30s
// - Environment variables: JWT_SECRET, DATABASE_URL, DB_USERNAME, DB_PASSWORD
// - IAM Role: LabRole (AWS Academy)

// Funcionalidades:
// 1. Recebe { "cpf": "12345678901", "authType": "customer" } OU { "authType": "anonymous" }
// 2. Valida CPF se fornecido
// 3. Busca cliente no RDS MySQL via JDBC
// 4. Gera JWT com dados do cliente ou sessionId para anônimos
// 5. Retorna { token, authType, cliente?, sessionId?, expiresIn }
// 6. Trata erros: CPF inválido (400), Cliente não encontrado (404), Erro interno (500)
```

### AuthenticationService.java
```java
// Lógica principal:
// - authenticate(AuthRequest): entry point
// - authenticateCustomer(String cpf): fluxo CPF
// - authenticateAnonymous(): fluxo anônimo
// - Validações: CPF format, cliente exists
// - Integração: ClienteService, JwtService, CpfValidator
```

### JwtService.java
```java
// JWT Management:
// - generateCustomerToken(Cliente): JWT com dados pessoais
// - generateAnonymousToken(String sessionId): JWT com sessionId
// - validateToken(String token): validação de assinatura e expiração
// - getClaims(String token): extração de dados
// - Algorithm: HMAC256
// - Issuer: "lanchonete-auth"
// - Expiration: 1 hora
```

### JWT Authorizer (Separado)
```java
// JwtAuthorizerHandler.java
// - Handler para API Gateway Custom Authorizer
// - Input: { "authorizationToken": "Bearer jwt...", "methodArn": "..." }
// - Output: IAM Policy + Context com dados do usuário
// - Context injetado: clienteId, cpf, nome, authType, sessionId
// - Headers injetados na aplicação: X-Cliente-ID, X-Cliente-CPF, X-Auth-Type, X-Session-ID
```

### ClienteService.java
```java
// Database Integration:
// - Conexão direta JDBC com RDS MySQL
// - buscarPorCpf(String cpf): SELECT * FROM clientes WHERE cpf = ?
// - Connection string via environment variable
// - Error handling para SQLException
```

## 🗄️ ESPECIFICAÇÃO DO BANCO DE DADOS

### Justificativa Técnica - MySQL
```
Por que MySQL:
✅ ACID Compliance: Essencial para pedidos e pagamentos
✅ Schema Relacional: Adequado para domínio lanchonete
✅ RDS Gerenciado: Backups, patches, monitoramento automáticos
✅ Performance Read-Heavy: Otimizado para consultas (produtos, pedidos)
✅ Custo-Benefício: Mais econômico que NoSQL gerenciados
✅ Maturidade: 25+ anos de estabilidade
✅ Skillset: Conhecimento amplamente disponível
```

### Schema Principal
```sql
-- Clientes (CPF único, campos obrigatórios)
CREATE TABLE clientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cpf (cpf)
);

-- Produtos (categorias fixas, preços decimais)
CREATE TABLE produtos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    categoria ENUM('LANCHE','BEBIDA','ACOMPANHAMENTO','SOBREMESA') NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_categoria (categoria),
    INDEX idx_ativo (ativo)
);

-- Pedidos (relacionamento opcional com cliente)
CREATE TABLE pedidos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id BIGINT NULL,  -- NULL para pedidos anônimos
    status ENUM('RECEBIDO','EM_PREPARACAO','PRONTO','FINALIZADO') DEFAULT 'RECEBIDO',
    status_pagamento ENUM('PENDENTE','APROVADO','REJEITADO') DEFAULT 'PENDENTE',
    valor_total DECIMAL(10,2) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL,
    INDEX idx_cliente (cliente_id),
    INDEX idx_status (status),
    INDEX idx_status_pagamento (status_pagamento),
    INDEX idx_data_criacao (data_criacao)
);

-- Itens do pedido (relacionamento N:N com produtos)
CREATE TABLE itens_pedido (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,  -- Snapshot do preço
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produtos(id),
    INDEX idx_pedido (pedido_id),
    INDEX idx_produto (produto_id)
);
```

### Dados de Exemplo (Seed)
```sql
-- Clientes de teste
INSERT INTO clientes (nome, email, cpf) VALUES
('João Silva', 'joao@email.com', '12345678901'),
('Maria Santos', 'maria@email.com', '98765432100'),
('Pedro Oliveira', 'pedro@email.com', '11122233344');

-- Produtos por categoria
INSERT INTO produtos (nome, categoria, preco, descricao) VALUES
-- Lanches
('Big Burguer', 'LANCHE', 25.90, 'Hambúrguer artesanal com carne de 200g'),
('Cheese Burguer', 'LANCHE', 22.50, 'Hambúrguer com queijo cheddar'),
('Bacon Burguer', 'LANCHE', 27.90, 'Hambúrguer com bacon crocante'),
-- Bebidas
('Coca-Cola', 'BEBIDA', 6.00, 'Refrigerante Coca-Cola 350ml'),
('Suco Natural', 'BEBIDA', 8.50, 'Suco natural de laranja 400ml'),
-- Acompanhamentos
('Batata Frita', 'ACOMPANHAMENTO', 8.90, 'Batata frita crocante'),
('Onion Rings', 'ACOMPANHAMENTO', 10.90, 'Anéis de cebola empanados'),
-- Sobremesas
('Sorvete', 'SOBREMESA', 12.90, 'Sorvete artesanal 2 bolas'),
('Brownie', 'SOBREMESA', 14.90, 'Brownie com calda de chocolate');
```

### Configuração RDS
```hcl
# terraform/database/rds.tf
resource "aws_db_instance" "mysql" {
  identifier = "lanchonete-mysql"
  engine = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"  # Free tier compatible
  
  allocated_storage = 20
  storage_type = "gp2"
  storage_encrypted = false  # Academy limitation
  
  db_name = "lanchonete"
  username = var.db_username
  password = var.db_password
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name = aws_db_subnet_group.default.name
  
  backup_retention_period = 7
  backup_window = "03:00-04:00"
  maintenance_window = "sun:04:00-sun:05:00"
  
  skip_final_snapshot = true  # Para ambiente de desenvolvimento
  deletion_protection = false
  
  tags = {
    Name = "lanchonete-mysql"
    Environment = "academy"
  }
}
```

## ☸️ ESPECIFICAÇÃO DO KUBERNETES

### Adaptações para AWS Academy
```yaml
# Mudanças principais da Fase 2 → Fase 3:
# 1. Services: NodePort → ClusterIP (acesso via ALB)
# 2. Security: Context injection via headers API Gateway
# 3. Database: MySQL local → RDS endpoint
# 4. Scaling: Mantido HPA (CPU/Memory based)
```

### Security Filter Adaptado
```java
// applications/autoatendimento/src/main/java/.../infra/ApiGatewayContextFilter.java
@Component
public class ApiGatewayContextFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {

        // Extrair context injetado pelo API Gateway JWT Authorizer
        String clienteId = request.getHeader("X-Cliente-ID");
        String cpf = request.getHeader("X-Cliente-CPF");
        String nome = request.getHeader("X-Cliente-Nome");
        String authType = request.getHeader("X-Auth-Type");
        String sessionId = request.getHeader("X-Session-ID");

        // Criar Authentication baseado no tipo
        if ("customer".equals(authType) && clienteId != null) {
            CustomerAuthenticationToken auth = new CustomerAuthenticationToken(
                Long.valueOf(clienteId), cpf, nome,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
        } else if ("anonymous".equals(authType) && sessionId != null) {
            AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(
                sessionId, "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
```

### ALB Ingress Controller
```yaml
# k8s-manifests/ingress/alb-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: lanchonete-alb
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internal
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
    alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
spec:
  rules:
    - http:
        paths:
          - path: /produtos
            pathType: Prefix
            backend:
              service:
                name: autoatendimento-service
                port:
                  number: 80
          - path: /pedidos
            pathType: Prefix
            backend:
              service:
                name: autoatendimento-service
                port:
                  number: 80
          - path: /clientes
            pathType: Prefix
            backend:
              service:
                name: autoatendimento-service
                port:
                  number: 80
          - path: /pagamentos
            pathType: Prefix
            backend:
              service:
                name: pagamento-service
                port:
                  number: 80
```

## 🌐 ESPECIFICAÇÃO DO API GATEWAY

### Estrutura de Routes
```
Públicas (sem autenticação):
POST /auth                    → Lambda (única rota pública)
GET  /health                 → Lambda (health check)

Protegidas (JWT Authorizer):
GET  /produtos/categoria/{categoria}  → EKS via VPC Link
POST /produtos                       → EKS (admin - futuro)
GET  /clientes/cpf/{cpf}            → EKS
POST /clientes                      → EKS
GET  /pedidos                       → EKS
GET  /pedidos/{id}                  → EKS
GET  /pedidos/cozinha               → EKS
POST /pedidos/checkout              → EKS
PUT  /pedidos/{id}/status           → EKS
POST /pagamentos                    → EKS
GET  /pagamentos/{id}/status        → EKS
POST /webhook/pagamento             → EKS (sem auth - webhook externo)
```

### Terraform API Gateway
```hcl
# terraform/lambda/api-gateway.tf
resource "aws_api_gateway_rest_api" "lanchonete_api" {
  name = "lanchonete-api"
  description = "API Gateway para sistema de lanchonete"
  
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

# JWT Custom Authorizer
resource "aws_api_gateway_authorizer" "jwt_authorizer" {
  name = "jwt-authorizer"
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  authorizer_uri = aws_lambda_function.jwt_authorizer.invoke_arn
  authorizer_credentials = data.aws_iam_role.lab_role.arn
  type = "TOKEN"
  identity_source = "method.request.header.Authorization"
  authorizer_result_ttl_in_seconds = 300  # Cache por 5 minutos
}

# VPC Link para EKS
resource "aws_api_gateway_vpc_link" "eks_vpc_link" {
  name = "eks-vpc-link"
  description = "VPC Link para conectar com EKS"
  target_arns = [aws_lb.alb.arn]
}
```

### Context Injection Configuration
```hcl
# Headers injetados pelo authorizer para aplicação
resource "aws_api_gateway_integration" "produtos_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.produtos_categoria_param.id
  http_method = aws_api_gateway_method.produtos_get.http_method

  type = "HTTP_PROXY"
  integration_http_method = "GET"
  uri = "http://${aws_lb.alb.dns_name}/produtos/categoria/{categoria}"
  
  connection_type = "VPC_LINK"
  connection_id = aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    "integration.request.path.categoria" = "method.request.path.categoria"
    "integration.request.header.X-Cliente-ID" = "context.authorizer.clienteId"
    "integration.request.header.X-Cliente-CPF" = "context.authorizer.cpf"
    "integration.request.header.X-Cliente-Nome" = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type" = "context.authorizer.authType"
    "integration.request.header.X-Session-ID" = "context.authorizer.sessionId"
  }
}
```

## 🚀 ESPECIFICAÇÃO DO CI/CD

### GitHub Actions Strategy
```yaml
# Path-based deployment com 4 workflows principais:

# 1. deploy-lambda.yml (paths: lambda-auth/**, terraform/lambda/**)
# - Maven build + test
# - JAR packaging
# - Terraform apply (Lambda + API Gateway)

# 2. deploy-infra-db.yml (paths: terraform/database/**)
# - Terraform plan/apply
# - Run migrations
# - Validate connectivity

# 3. deploy-infra-k8s.yml (paths: terraform/kubernetes/**, k8s-manifests/**)
# - Terraform apply (EKS + VPC Link + ALB)
# - Update kubectl config
# - Apply K8s manifests

# 4. deploy-applications.yml (paths: applications/**)
# - Multi-stage Docker build
# - Push to ECR
# - Rolling update in EKS
```

### Secrets Management
```yaml
# GitHub Repository Secrets necessários:
AWS_ACCESS_KEY_ID         # AWS Academy
AWS_SECRET_ACCESS_KEY     # AWS Academy  
AWS_SESSION_TOKEN         # AWS Academy (obrigatório)
JWT_SECRET                # Chave para assinar JWTs
DB_USERNAME               # Username RDS
DB_PASSWORD               # Password RDS
SOAT_USER_ACCESS          # Para adicionar soatarchitecture
```

### Branch Protection Rules
```yaml
# main branch:
required_status_checks:
  - "ci/lambda-tests"
  - "ci/terraform-plan-db" 
  - "ci/terraform-plan-k8s"
  - "ci/app-tests"
required_pull_request_reviews:
  required_approving_review_count: 1
enforce_admins: true
restrict_pushes: true
```

## 🧪 ESPECIFICAÇÃO DE TESTES

### Teste Completo via cURL
```bash
#!/bin/bash
# scripts/test-complete-flow.sh

API_BASE="https://api.lanchonete.com"

echo "🔑 1. AUTENTICAÇÃO COM CPF"
CUSTOMER_RESPONSE=$(curl -s -X POST "$API_BASE/auth" \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901", "authType": "customer"}')
  
CUSTOMER_TOKEN=$(echo $CUSTOMER_RESPONSE | jq -r '.token')

echo "👤 2. AUTENTICAÇÃO ANÔNIMA" 
ANON_RESPONSE=$(curl -s -X POST "$API_BASE/auth" \
  -H "Content-Type: application/json" \
  -d '{"authType": "anonymous"}')
  
ANON_TOKEN=$(echo $ANON_RESPONSE | jq -r '.token')

echo "🍔 3. LISTAR PRODUTOS (Cliente)"
curl -X GET "$API_BASE/produtos/categoria/LANCHE" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | jq .

echo "🍔 4. LISTAR PRODUTOS (Anônimo)"
curl -X GET "$API_BASE/produtos/categoria/LANCHE" \
  -H "Authorization: Bearer $ANON_TOKEN" | jq .

echo "🛒 5. CHECKOUT CLIENTE"
PEDIDO_CUSTOMER=$(curl -s -X POST "$API_BASE/pedidos/checkout" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"itens": [{"produtoId": 1, "quantidade": 1}]}')
  
echo "🛒 6. CHECKOUT ANÔNIMO"
PEDIDO_ANON=$(curl -s -X POST "$API_BASE/pedidos/checkout" \
  -H "Authorization: Bearer $ANON_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"itens": [{"produtoId": 2, "quantidade": 1}]}')

echo "💳 7. PROCESSAR PAGAMENTOS"
# Processar ambos pagamentos

echo "📊 8. CONSULTAR COZINHA"
curl -X GET "$API_BASE/pedidos/cozinha" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | jq .
```

### Cenários de Validação
```
✅ Autenticação CPF válido → JWT com dados pessoais
✅ Autenticação anônima → JWT com sessionId
✅ JWT inválido → 401 Unauthorized
✅ JWT expirado → 401 Unauthorized
✅ Produtos → Mesma resposta para ambos tipos
### Funcionalidade Requirements  
```yaml
✅ Autenticação CPF gerando JWT com dados pessoais
✅ Autenticação anônima gerando JWT com sessionId
✅ Context injection funcionando (headers X-*)
✅ CRUD produtos com autenticação
✅ Checkout identificado (clienteId preenchido)
✅ Checkout anônimo (clienteId null)
✅ Processamento pagamentos via mock
✅ Webhook pagamento funcionando
✅ Consulta pedidos cozinha
✅ Fluxo completo via cURL testado
```

### Vídeo Requirements
```yaml
✅ Duração 45-60 minutos
✅ Mostra todos serviços AWS criados
✅ Explica função de cada componente
✅ Demonstra aplicação funcionando end-to-end
✅ Exibe GitHub Actions com checks verdes
✅ Mostra Pull Requests fechados
✅ Justifica escolhas técnicas (MySQL, sem Cognito)
✅ Evidencia adaptações AWS Academy
✅ Upload YouTube/Vimeo (público ou não listado)
✅ Link disponível no repositório
```

### Documentation Requirements
```yaml
✅ Justificativa técnica escolha MySQL
✅ Explicação por que não usar Cognito  
✅ Documentação adaptações AWS Academy
✅ Diagramas arquiteturais
✅ Guias de setup e troubleshooting
✅ Modelagem de dados documentada
✅ APIs documentadas (Swagger/OpenAPI)
✅ Context injection pattern explicado
```

## 🎯 CRITÉRIOS DE SUCESSO

### Técnicos
```yaml
✅ Sistema funcionando 100% end-to-end via cURL
✅ Autenticação dual (CPF + anônimo) funcionando
✅ JWT Authorizer validando e injetando context
✅ Aplicações recebendo dados corretos via headers
✅ Database queries funcionando (RDS MySQL)
✅ CI/CD executando sem erros
✅ Infraestrutura toda via Terraform
✅ Zero hardcoded secrets
```

### Demonstração
```yaml
✅ Professor consegue reproduzir todos testes
✅ APIs respondem corretamente via cURL
✅ Context é injetado corretamente (logs evidenciam)
✅ Cliente identificado vs anônimo funcionam diferentes
✅ Pedidos ficam vinculados corretamente ao cliente
✅ GitHub Actions mostra deployments automáticos
✅ Arquitetura AWS visível e explicada
```

### Qualidade
```yaml
✅ Código limpo e bem estruturado
✅ Arquitetura respeitando Clean Architecture
✅ Logs estruturados para debugging
✅ Error handling adequado
✅ Performance otimizada para ambiente Academy
✅ Security best practices aplicadas
✅ Documentação completa e clara
```

---

## 📝 LOG DE ATUALIZAÇÕES AUTOMÁTICAS
*Esta seção será mantida pelo Claude Code para rastrear mudanças de status e commits*

### Status das Etapas
```
Data       | Etapa | Status Anterior | Status Novo | Usuário
-----------|-------|-----------------|-------------|--------
2025-08-30 | ETAPA 1 | [ ] TODO      | [X] DONE   | Usuario
```

### Histórico de Commits
```
Data       | Mensagem Sugerida                           | Status
-----------|---------------------------------------------|--------
(Aguardando primeiro grupo de artefatos)
```

---

## 🚀 COMANDO PARA CLAUDE CODE

Para gerar todos os artefatos com Claude Code, execute:

```bash
claude-code generate --from=CLAUDE.md --target=lanchonete-tech-challenge-fase3/
```

Este arquivo CLAUDE.md contém todas as especificações técnicas completas para implementar o Tech Challenge Fase 3 com arquitetura cloud-native na AWS Academy. cliente → clienteId preenchido
✅ Checkout anônimo → clienteId = null
✅ Headers injetados → Context correto na aplicação
✅ Pagamentos → Webhook funcionando
✅ Cozinha → Lista todos pedidos
```

## 🎬 ESPECIFICAÇÃO DO VÍDEO

### Roteiro (45-60 minutos)
```
00:00-05:00  Introdução
             - Apresentação da equipe
             - Objetivos da Fase 3
             - Arquitetura geral na AWS

05:00-15:00  AWS Console Tour
             - API Gateway (routes, authorizer)
             - Lambda functions (logs, test)
             - EKS cluster (nodes, pods)
             - RDS MySQL (endpoint, security)
             - VPC Link (connection)

15:00-25:00  Demonstração via cURL
             - Autenticação CPF + anônimo
             - Listagem produtos
             - Checkout ambos fluxos
             - Processamento pagamentos
             - Consulta cozinha

25:00-35:00  GitHub Actions
             - Mostrar workflows (checks verdes)
             - Pull requests fechados
             - Deploy automático
             - Secrets configurados

35:00-45:00  Explicação Técnica
             - Por que não Cognito
             - Justificativa MySQL
             - Adaptações AWS Academy
             - Context injection pattern

45:00-50:00  Considerações Finais
             - Lições aprendidas
             - Próximos passos
             - Limpeza de recursos
```

### Pontos Obrigatórios
```
✅ Mostrar serviços criados na cloud
✅ Explicar função de cada serviço
✅ Demonstrar aplicação funcionando
✅ Exibir pipelines CI/CD (checks verdes)
✅ Justificar escolhas arquiteturais
✅ Evidenciar branch protection
✅ Mostrar Pull Requests fechados
```

## ⚙️ CONFIGURAÇÕES AWS ACADEMY

### Adaptações Obrigatórias
```yaml
# Todas as resources devem usar:
iam_role: data.aws_iam_role.lab_role.arn
region: us-east-1
vpc: data.aws_vpc.default (VPC padrão)

# Limitações:
- Não pode criar IAM roles customizadas
- Não pode criar VPCs customizadas  
- Sessão expira em 4 horas
- Região limitada a us-east-1
- Budget limitado (~$100 por sessão)
```

### Terraform Academy Configuration
```hcl
# terraform/shared/variables.tf
variable "aws_region" {
  description = "AWS region (fixed for Academy)"
  type        = string
  default     = "us-east-1"
}

variable "lab_session_id" {
  description = "Academy lab session identifier"
  type        = string
  default     = ""
}

# terraform/shared/academy.tfvars
aws_region = "us-east-1"
lab_session_id = "academy-session-2024"
db_username = "lanchonete"
db_password = "LanchoneteAcademy123!"
jwt_secret = "academy-jwt-secret-super-strong-key-12345"
```

### Data Sources Obrigatórios
```hcl
# terraform/shared/data.tf
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}
```

## 📊 ESPECIFICAÇÃO DE MONITORAMENTO

### CloudWatch Integration
```yaml
# Logs obrigatórios para demonstração:
/aws/lambda/lanchonete-auth              # Lambda auth logs
/aws/apigateway/lanchonete-api           # API Gateway access logs
/aws/eks/lanchonete-cluster/cluster      # EKS cluster logs
/aws/rds/instance/lanchonete-mysql/error # RDS error logs

# Métricas importantes:
- Lambda: Duration, Errors, Throttles
- API Gateway: Count, Latency, 4xx/5xx errors  
- EKS: CPU, Memory, Pod restarts
- RDS: Connections, CPU, Read/Write IOPS
```

### Application Logs Pattern
```java
// Logs estruturados para troubleshooting
// lambda-auth/src/main/java/.../AuthHandler.java
@Override
public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    String requestId = context.getAwsRequestId();
    
    try {
        logger.info("Iniciando processamento de autenticacao - RequestId: {}", requestId);
        logger.debug("Corpo da requisicao recebido: {}", input.getBody());
        
        AuthRequest request = objectMapper.readValue(input.getBody(), AuthRequest.class);
        AuthResponse response = authService.authenticate(request);
        
        logger.info("Autenticacao realizada com sucesso - RequestId: {}, Tipo: {}, ClienteId: {}", 
                   requestId, response.getAuthType(), 
                   response.getCliente() != null ? response.getCliente().getId() : "null");
        
        return createSuccessResponse(response);
        
    } catch (ClienteNotFoundException e) {
        logger.warn("Cliente nao encontrado - RequestId: {}, CPF: {}", requestId, maskCpf(extractCpf(input)));
        return createErrorResponse(404, "Cliente não encontrado", e.getMessage());
    } catch (Exception e) {
        logger.error("Erro interno na autenticacao - RequestId: {}, Erro: {}", requestId, e.getMessage(), e);
        return createErrorResponse(500, "Erro interno", "Verifique os logs");
    }
}
```

## 🔒 ESPECIFICAÇÃO DE SEGURANÇA

### JWT Security Configuration
```java
// lambda-auth/src/main/java/.../JwtService.java
@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    // Configurações de segurança
    private static final String ALGORITHM = "HMAC256";
    private static final String ISSUER = "lanchonete-auth";
    private static final int EXPIRATION_SECONDS = 3600; // 1 hora
    
    @Value("${jwt.secret}")
    private String jwtSecret; // Mínimo 256 bits para HMAC256
    
    public String generateCustomerToken(Cliente cliente) {
        logger.debug("Gerando token JWT para cliente ID: {}", cliente.getId());
        
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(cliente.getId().toString())
                .withClaim("cpf", cliente.getCpf())
                .withClaim("nome", cliente.getNome()) 
                .withClaim("email", cliente.getEmail())
                .withClaim("type", "customer")
                .withClaim("role", "CUSTOMER")
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(EXPIRATION_SECONDS)))
                .sign(Algorithm.HMAC256(jwtSecret));
    }
    
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(ISSUER)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            
            // Verificações adicionais
            if (jwt.getExpiresAt().before(new Date())) {
                logger.warn("Token expirado detectado");
                return false;
            }
            
            return true;
        } catch (JWTVerificationException e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }
}
```

### Database Security
```sql
-- Configurações de segurança MySQL
-- terraform/database/migrations/001_create_schema.sql

-- Criar usuário específico da aplicação (não root)
CREATE USER 'lanchonete_app'@'%' IDENTIFIED BY 'ComplexPassword123!';

-- Permissões mínimas necessárias
GRANT SELECT, INSERT, UPDATE ON lanchonete.clientes TO 'lanchonete_app'@'%';
GRANT SELECT ON lanchonete.produtos TO 'lanchonete_app'@'%';
GRANT SELECT, INSERT, UPDATE ON lanchonete.pedidos TO 'lanchonete_app'@'%';
GRANT SELECT, INSERT ON lanchonete.itens_pedido TO 'lanchonete_app'@'%';

-- Flush privileges
FLUSH PRIVILEGES;

-- Índices para performance e segurança
CREATE INDEX idx_cpf_hash ON clientes(cpf); -- Busca rápida por CPF
CREATE INDEX idx_pedido_data ON pedidos(data_criacao); -- Consultas por data
CREATE INDEX idx_pedido_status ON pedidos(status, status_pagamento); -- Cozinha
```

### Network Security
```hcl
# terraform/database/security-groups.tf
resource "aws_security_group" "rds" {
  name_prefix = "lanchonete-rds-"
  description = "Security group for RDS MySQL"
  vpc_id      = data.aws_vpc.default.id

  # Apenas Lambda e EKS podem acessar
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/8"] # VPC interna apenas
  }
  
  # Sem acesso externo direto
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = {
    Name = "lanchonete-rds-sg"
  }
}

resource "aws_security_group" "lambda" {
  name_prefix = "lanchonete-lambda-"
  description = "Security group for Lambda functions"
  vpc_id      = data.aws_vpc.default.id

  # Lambda precisa acessar RDS
  egress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp" 
    cidr_blocks = ["10.0.0.0/8"]
  }
  
  # Lambda precisa internet para logs CloudWatch
  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
```

## 📈 ESPECIFICAÇÃO DE PERFORMANCE

### Lambda Optimization
```yaml
# Configuração otimizada para performance
Runtime: java17
Memory: 512MB                    # Balanço custo/performance
Timeout: 30s                     # Suficiente para DB queries
Reserved Concurrency: 10        # Controle de custos
Provisioned Concurrency: 2      # Reduzir cold starts (opcional)

# Otimizações JVM
Environment:
  JAVA_TOOL_OPTIONS: >-
    -XX:+TieredCompilation
    -XX:TieredStopAtLevel=1
    -Dspring.main.lazy-initialization=true
    -Dspring.jmx.enabled=false
```

### EKS Performance Configuration
```yaml
# k8s-manifests/applications/autoatendimento-deployment.yaml
spec:
  replicas: 2                    # Mínimo para HA
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0          # Zero downtime
      maxSurge: 1

  template:
    spec:
      containers:
        - name: app
          resources:
            requests:
              memory: "256Mi"    # Baseado em profiling
              cpu: "250m"        # 0.25 CPU
            limits:
              memory: "512Mi"    # Limite conservador  
              cpu: "500m"        # 0.5 CPU max
          
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
```

### Database Performance
```sql
-- Configurações de performance MySQL
-- terraform/database/rds.tf parameter group
resource "aws_db_parameter_group" "mysql_params" {
  family = "mysql8.0"
  
  parameter {
    name  = "innodb_buffer_pool_size"
    value = "{DBInstanceClassMemory*3/4}"  # 75% da RAM
  }
  
  parameter {
    name  = "max_connections"
    value = "100"  # Suficiente para carga esperada
  }
  
  parameter {
    name  = "query_cache_type"
    value = "1"   # Enable query cache
  }
  
  parameter {
    name  = "query_cache_size"
    value = "33554432"  # 32MB
  }
}
```

## 🛠️ ESPECIFICAÇÃO DE SCRIPTS UTILITÁRIOS

### Setup Script
```bash
#!/bin/bash
# scripts/setup-aws-academy.sh

set -e

echo "🎓 CONFIGURANDO AMBIENTE AWS ACADEMY"
echo "===================================="

# 1. Verificar AWS CLI
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI não encontrado. Instale primeiro."
    exit 1
fi

# 2. Verificar credenciais Academy
echo "🔍 Verificando credenciais AWS Academy..."
CALLER_IDENTITY=$(aws sts get-caller-identity 2>/dev/null || echo "error")

if [[ $CALLER_IDENTITY == "error" ]]; then
    echo "❌ Credenciais AWS não configuradas!"
    echo "💡 No AWS Academy:"
    echo "   1. Acesse 'AWS Details'"
    echo "   2. Clique em 'AWS CLI'"
    echo "   3. Copie e execute os comandos export"
    exit 1
fi

# 3. Verificar se é LabRole
ROLE_ARN=$(echo $CALLER_IDENTITY | jq -r '.Arn')
if [[ $ROLE_ARN == *"LabRole"* ]]; then
    echo "✅ LabRole detectada: OK"
else
    echo "⚠️  Warning: Não está usando LabRole - pode haver limitações"
fi

# 4. Configurar região padrão
export AWS_DEFAULT_REGION=us-east-1
echo "✅ Região configurada: $AWS_DEFAULT_REGION"

# 5. Verificar Terraform
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform não encontrado. Instalando..."
    # Script de instalação do Terraform aqui
fi

TERRAFORM_VERSION=$(terraform version -json | jq -r '.terraform_version')
echo "✅ Terraform versão: $TERRAFORM_VERSION"

# 6. Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl não encontrado. Instalando..."
    # Script de instalação do kubectl aqui
fi

# 7. Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não encontrado. Necessário para build."
    exit 1
fi

echo ""
echo "✅ AMBIENTE CONFIGURADO COM SUCESSO!"
echo "💡 Próximos passos:"
echo "   1. ./scripts/deploy-all.sh"
echo "   2. ./scripts/test-complete-flow.sh"
echo ""
echo "⚠️  LEMBRE-SE: Credenciais AWS Academy expiram em 4 horas!"
```

### Deploy All Script
```bash
#!/bin/bash
# scripts/deploy-all.sh

set -e

echo "🚀 DEPLOY COMPLETO - TECH CHALLENGE FASE 3"
echo "=========================================="

# 1. Deploy Database
echo "🗄️ 1/4 - DEPLOYANDO INFRAESTRUTURA DO BANCO..."
cd terraform/database
terraform init -upgrade
terraform plan -var-file="../shared/academy.tfvars"
terraform apply -var-file="../shared/academy.tfvars" -auto-approve

# Aguardar RDS ficar disponível
echo "⏳ Aguardando RDS ficar disponível (pode demorar 10-15 min)..."
sleep 300  # 5 minutos inicial
cd ../..

# 2. Build Lambda
echo "⚡ 2/4 - BUILDING E DEPLOYANDO LAMBDA..."
cd lambda-auth
mvn clean package -DskipTests
cd ../terraform/lambda
terraform init -upgrade
terraform plan -var-file="../shared/academy.tfvars"
terraform apply -var-file="../shared/academy.tfvars" -auto-approve
cd ../..

# 3. Deploy Kubernetes
echo "☸️ 3/4 - DEPLOYANDO EKS (pode demorar 20-25 min)..."
cd terraform/kubernetes
terraform init -upgrade
terraform plan -var-file="../shared/academy.tfvars"
terraform apply -var-file="../shared/academy.tfvars" -auto-approve

# Configurar kubectl
CLUSTER_NAME=$(terraform output -raw cluster_name)
aws eks update-kubeconfig --region us-east-1 --name $CLUSTER_NAME
cd ../..

# Aguardar cluster ficar pronto
echo "⏳ Aguardando cluster EKS ficar pronto..."
kubectl wait --for=condition=Ready nodes --all --timeout=600s

# 4. Deploy Applications
echo "📦 4/4 - DEPLOYANDO APLICAÇÕES..."

# Build e push imagens (se ECR estiver configurado)
# docker build -t autoatendimento applications/autoatendimento/
# docker build -t pagamento applications/pagamento/

# Deploy manifests
kubectl apply -f k8s-manifests/configmaps/
kubectl apply -f k8s-manifests/secrets/ 
kubectl apply -f k8s-manifests/applications/
kubectl apply -f k8s-manifests/hpa/
kubectl apply -f k8s-manifests/ingress/

# Aguardar pods ficarem prontos
echo "⏳ Aguardando aplicações ficarem prontas..."
kubectl wait --for=condition=ready pod -l app=autoatendimento --timeout=300s
kubectl wait --for=condition=ready pod -l app=pagamento --timeout=300s

echo ""
echo "✅ DEPLOY COMPLETO FINALIZADO!"
echo "=============================="

# Mostrar URLs
API_GATEWAY_URL=$(cd terraform/lambda && terraform output -raw api_gateway_url)
echo "🌐 API Gateway URL: $API_GATEWAY_URL"

ALB_ENDPOINT=$(kubectl get ingress lanchonete-alb -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo "📡 ALB Endpoint: $ALB_ENDPOINT"

echo ""
echo "🧪 PRÓXIMO PASSO: Executar testes"
echo "   ./scripts/test-complete-flow.sh"
```

### Cleanup Script
```bash
#!/bin/bash
# scripts/cleanup-all.sh

set -e

echo "🧹 LIMPEZA COMPLETA DE RECURSOS AWS"
echo "=================================="

# 1. Limpar Kubernetes
echo "☸️ 1/4 - REMOVENDO RECURSOS KUBERNETES..."
if command -v kubectl &> /dev/null; then
    kubectl delete --all pods --grace-period=0 --force || true
    kubectl delete --all services || true
    kubectl delete --all deployments || true
    kubectl delete --all configmaps || true
    kubectl delete --all secrets || true
    kubectl delete --all ingress || true
    kubectl delete --all hpa || true
fi

# 2. Destroy Kubernetes Infrastructure
echo "🏗️ 2/4 - DESTRUINDO INFRAESTRUTURA EKS..."
cd terraform/kubernetes
terraform destroy -var-file="../shared/academy.tfvars" -auto-approve || true
cd ../..

# 3. Destroy Lambda
echo "⚡ 3/4 - DESTRUINDO LAMBDA E API GATEWAY..."
cd terraform/lambda
terraform destroy -var-file="../shared/academy.tfvars" -auto-approve || true
cd ../..

# 4. Destroy Database (last)
echo "🗄️ 4/4 - DESTRUINDO BANCO DE DADOS..."
cd terraform/database
terraform destroy -var-file="../shared/academy.tfvars" -auto-approve || true
cd ../..

echo ""
echo "✅ LIMPEZA COMPLETA FINALIZADA!"
echo "=============================="
echo "💰 Recursos AWS removidos - custos zerados"
echo "🎓 Academy budget preservado"
```

## 📝 CHECKLIST DE ENTREGA

### Repositório Requirements
```yaml
✅ Repositório privado criado
✅ User soatarchitecture adicionado com acesso
✅ Branch main protegida (require PR + reviews)
✅ README.md principal completo e detalhado
✅ VIDEO.md com link do YouTube/Vimeo
✅ Estrutura monorepo organizada
✅ .gitignore configurado (não commitar secrets)
✅ Pull Requests fechados (evidência de proteção)
```

### Código Requirements
```yaml
✅ Lambda Auth em Java 17 completa
✅ Aplicações migradas da Fase 2 (Clean Architecture)
✅ Terraform para toda infraestrutura (adaptado LabRole)
✅ Kubernetes manifests adaptados (ClusterIP + ALB)
✅ GitHub Actions workflows (path-based deployment)
✅ Scripts utilitários completos
✅ Testes unitários (mínimo 70% cobertura)
✅ Documentação técnica detalhada
```

### Infraestrutura Requirements
```yaml
✅ API Gateway com routes completas + JWT Authorizer
✅ Lambda de autenticação deployada e funcionando
✅ EKS cluster com aplicações rodando
✅ RDS MySQL com dados de seed
✅ VPC Link conectando API Gateway ↔ EKS
✅ ALB roteando tráfego interno
✅ Security Groups configurados
✅ CloudWatch logs habilitados
```

### Funcionalidade Requirements  
```yaml
✅ Autenticação CPF gerando JWT com dados pessoais
✅ Autenticação anônima gerando JWT com sessionId
✅ Context injection funcionando (headers X-*)
✅ CRUD produtos com autenticação
✅ Checkout
- comandos de destroy só podem ser executados pelo usuário
- a cada nova sessão, acesse a aws para saber se tem algum recurso rodando e avise o usuário
- nenhum comando com -auto-approve
- a cada inicio de sessao, verifique se há recursos órfãos na aws que podem ser deletados
- all messages in brazilian portuguese