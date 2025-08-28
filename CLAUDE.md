# 🚀 Plano de Migração Serverless - Lanchonete Autoatendimento

## 🇧🇷 IDIOMA E CONVENÇÕES DE CÓDIGO

**IMPORTANTE:** 
- ✅ **Toda comunicação** deve ser feita em **português brasileiro**
- ✅ **Código em português brasileiro** sempre que possível (nomes de classes, métodos, variáveis)
- ✅ **Manter em inglês apenas** termos técnicos universais (handler, request, response, exception, gateway, service)
- ✅ **Comentários de código** sempre em português brasileiro
- ✅ **Documentação** sempre em português brasileiro
- ✅ **Mensagens de erro** sempre em português brasileiro

## 🔄 REGRA DE REUSO DE CÓDIGO

**PRIORIDADE MÁXIMA: Copiar código existente sempre que possível**
- ✅ **SEMPRE copiar** entities, enums, exceptions, DTOs, mappers do projeto atual
- ✅ **SEMPRE copiar** casos de uso existentes sem alterar lógica de negócio
- ✅ **APENAS adaptar** imports e nomes de pacotes para português brasileiro
- ✅ **MANTER** estrutura de dados, validações e comportamentos idênticos
- ✅ **EVITAR** reimplementar funcionalidades que já existem e funcionam

**Benefícios:**
- ✅ Consistência entre APIs REST e Serverless
- ✅ Reutilização de código validado e testado  
- ✅ Aceleração do desenvolvimento
- ✅ Menor probabilidade de bugs
- ✅ Facilita manutenção futura

### Exemplos de Nomenclatura:
```java
// ✅ CORRETO - Português brasileiro + termos técnicos
public class AutenticacaoCpfHandler {
    private final IdentificarClienteUseCase identificarCliente;
    
    public ResponseAuth autenticarPorCpf(RequestAuth request) {
        // Processar autenticação do cliente
    }
}

// ❌ EVITAR - Totalmente em inglês
public class AuthCpfHandler {
    private final IdentifyClientUseCase identifyClient;
    
    public AuthResponse authenticateByCpf(AuthRequest request) {
        // Process client authentication
    }
}
```

## 📋 Contexto do Projeto

**Aplicação atual:** Sistema de autoatendimento para lanchonete rodando em Kubernetes com Spring Boot + MySQL
**Objetivo:** Migrar para arquitetura serverless na AWS conforme **Tech Challenge Fase 3**
**Ambiente:** AWS Academy com LabRole
**Estratégia:** Migração progressiva, começando com autenticação CPF
**Base:** Especificações detalhadas no PDF do Tech Challenge Fase 3

## 📊 CONTROLE DE STATUS EM TEMPO REAL

**IMPORTANTE:** Este documento deve ser usado junto com o arquivo `STATUS_ATUAL.txt` que fica em cada repositório/fase específica.

### **Regras para STATUS_ATUAL.txt:**
- ✅ **DEVE ser atualizado constantemente** durante o desenvolvimento
- ✅ **DEVE refletir exatamente onde estamos na FASE ATUAL**
- ✅ **DEVE ser o primeiro arquivo a consultar** quando trocar de computador/sessão
- ✅ **DEVE conter** informações práticas: último commit, próximos passos, decisões pendentes
- ✅ **DEVE ser usado como contexto principal** pelo Claude Code em novas sessões

### **Localização do STATUS_ATUAL.txt:**
- `lambda-auth-cpf/STATUS_ATUAL.txt` - FASE 1: Autenticação CPF
- `lambda-produtos/STATUS_ATUAL.txt` - FASE 2: CRUD Produtos
- `lambda-pedidos/STATUS_ATUAL.txt` - FASE 4: Gestão de Pedidos
- `terraform-rds/STATUS_ATUAL.txt` - FASE 3: RDS Setup

### **Formato padrão do STATUS_ATUAL.txt:**
```
FASE ATUAL: [Nome da fase]
STATUS: [CONCLUÍDA/EM DESENVOLVIMENTO/PENDENTE]
ÚLTIMA ATUALIZAÇÃO: [Data e hora]
PRÓXIMO PASSO: [Ação específica a tomar]
DECISÕES PENDENTES: [Lista de decisões que precisam ser tomadas]
COMANDOS IMPORTANTES: [Comandos específicos para continuar o trabalho]
```

**⚠️ ATENÇÃO:** Sempre consulte o `STATUS_ATUAL.txt` da fase específica antes de fazer qualquer pergunta ou continuação do trabalho.

### **📄 Requisitos do Tech Challenge (PDF)**
A migração deve atender **obrigatoriamente** aos seguintes requisitos especificados no PDF:

#### **1. API Gateway + Function Serverless (Obrigatório)**
- ✅ Implementar API Gateway para receber solicitações externas
- ✅ Function serverless para autenticar/consultar cliente com base no CPF
- ✅ Cliente se identifica APENAS com CPF (sem senha)
- ✅ Fluxo de integração/consulta pode utilizar JWT ou equivalente

#### **2. Boas Práticas CI/CD (Futuro)**
- ✅ Segregar códigos em repositórios separados:
  - 1 repositório para Lambda de autenticação
  - 1 repositório para infraestrutura de banco de dados com Terraform
  - 1 repositório para aplicação principal (se mantida)
- ✅ Deploy automatizado usando GitHub Actions
- ✅ Branches main/master protegidas com Pull Request obrigatório
- ✅ Secrets para valores sensíveis
- ✅ Toda infraestrutura via Terraform

#### **3. Serviços Serverless Obrigatórios**
- ✅ **Functions:** AWS Lambda (escolhido)
- ✅ **Banco gerenciável:** AWS RDS MySQL (escolhido)
- ✅ **Autenticação:** Sistema próprio com JWT (sem Cognito conforme decisão)

#### **4. Melhorias de Banco de Dados**
- ✅ Documentar estrutura seguindo padrões de modelagem
- ✅ Justificar escolha do banco de dados (MySQL → RDS)
- ✅ Migração completa da estrutura atual

#### **5. Entregáveis Obrigatórios**
- ✅ Código-fonte em repositórios privados com acesso para `soatarchitecture`
- ✅ Vídeo demonstrativo da arquitetura em cloud
- ✅ Execução de pipelines CI/CD (checks verdes)
- ✅ Serviços funcionando em pleno funcionamento
- ✅ Explicação da função e escolha de cada serviço/arquitetura

### **🎯 Priorização Baseada no PDF**
**FASE 1 é obrigatória** conforme especificação:
1. **API Gateway** ← Requisito explícito
2. **Lambda de autenticação CPF** ← Requisito explícito  
3. **Sem sistema de senha** ← Requisito explícito
4. **JWT para autenticação** ← Sugerido no PDF

## 🎓 Configuração AWS Academy

### **Pré-requisitos AWS Academy**
- Acesso ao AWS Academy Learner Lab
- Conhecimento sobre credenciais temporárias
- LabRole disponível no ambiente

### **Configuração de Credenciais**
```bash
# 1. Iniciar lab no AWS Academy
# 2. Clicar em "AWS Details"
# 3. Configurar credenciais (expiram em 4-12h)
aws configure set aws_access_key_id AKIA...
aws configure set aws_secret_access_key wJal...
aws configure set aws_session_token IQoJ...
aws configure set region us-east-1

# 4. Verificar configuração
aws sts get-caller-identity
aws iam get-role --role-name LabRole
```

### **Limitações AWS Academy**
- ✅ Regiões disponíveis: us-east-1, us-west-2 (geralmente)
- ✅ Lambda Memory: máximo 1GB
- ✅ Lambda Timeout: máximo 60 segundos
- ✅ Credenciais temporárias (renovar periodicamente)
- ✅ RDS instance: db.t3.micro (Free Tier)
- ⚠️ Sem cobrança real - ideal para aprendizado

## 🎯 Arquitetura Alvo

```
Internet → API Gateway → Lambda Functions → RDS MySQL
                    ↓
               JWT Authentication
                    ↓
              Clean Architecture
                    ↓
                LabRole (AWS Academy)
```

## 📅 Fases da Migração

### **FASE 1: Autenticação CPF (OBRIGATÓRIA - PDF)**
**Duração estimada:** 1-2 dias
**Objetivo:** Atender requisitos obrigatórios do Tech Challenge
**Base:** Item 1 do PDF - "API Gateway + function serverless para autenticar cliente por CPF"

#### Estrutura do Projeto
```
lambda-auth-cpf/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── br/com/lanchonete/auth/
│   │           ├── AutenticacaoCpfHandler.java          # Handler principal
│   │           ├── domain/
│   │           │   ├── entities/Cliente.java
│   │           │   ├── valueobjects/Cpf.java
│   │           │   └── services/ServicoAutenticacao.java
│   │           ├── application/
│   │           │   ├── usecases/IdentificarCliente.java
│   │           │   └── gateways/ClienteGateway.java
│   │           └── adapters/
│   │               ├── jwt/ServicoJwt.java
│   │               └── mock/ClienteMockGateway.java
│   └── test/
├── pom.xml
├── terraform/
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
└── README.md
```

#### Funcionalidades (Conforme PDF)
- ✅ **API Gateway** para receber CPF via requisição externa (requisito PDF)
- ✅ **Function serverless** para processar autenticação (requisito PDF)
- ✅ **Identificação APENAS por CPF** - sem senha (requisito PDF)
- ✅ **JWT** para fluxo de integração/consulta (sugestão PDF)
- ✅ Validar formato do CPF (reutilizar código atual)
- ✅ Consultar cliente (dados mocados inicialmente)
- ✅ Retornar resposta padronizada

#### Request/Response
```json
// Request
POST /auth/cpf
{
  "cpf": "12345678901"
}

// Response
{
  "success": true,
  "cliente": {
    "id": 1,
    "cpf": "12345678901",
    "nome": "João Silva",
    "email": "joao@email.com"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

#### Dados Mock Iniciais
```java
// Clientes pré-cadastrados para teste
private static final List<Cliente> CLIENTES_MOCK = Arrays.asList(
    new Cliente(1L, "12345678901", "João Silva", "joao@email.com"),
    new Cliente(2L, "11144477735", "Maria Santos", "maria@email.com"),
    new Cliente(3L, "98765432100", "Pedro Costa", "pedro@email.com")
);
```

#### Configuração Terraform
- API Gateway REST API
- Lambda Function (Java 17, max 1GB memory para Academy)
- CloudWatch Logs
- **LabRole** (pré-configurada no AWS Academy)
- Outputs para testes

#### Configuração específica para AWS Academy
```hcl
# terraform/main.tf
# Usar LabRole existente ao invés de criar role própria
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

resource "aws_lambda_function" "autenticacao_cpf" {
  filename         = "../target/lambda-auth-cpf-1.0.0.jar"
  function_name    = "${var.nome_projeto}-autenticacao-cpf"
  role            = data.aws_iam_role.lab_role.arn  # Usar LabRole!
  handler         = "br.com.lanchonete.auth.AutenticacaoCpfHandler::processarRequisicao"
  runtime         = "java17"
  timeout         = 30  # Dentro dos limites do Academy
  memory_size     = 512 # Conservador para Academy
  
  environment {
    variables = {
      AMBIENTE = var.ambiente
      JWT_SECRET  = var.jwt_secret
    }
  }
}
```

#### Variáveis específicas para Academy
```hcl
# terraform/variables.tf
variable "regiao_aws" {
  description = "Região AWS (limitada no Academy)"
  type        = string
  default     = "us-east-1"
  
  validation {
    condition = contains(["us-east-1", "us-west-2"], var.regiao_aws)
    error_message = "Academy normalmente disponibiliza apenas us-east-1 ou us-west-2."
  }
}

variable "memoria_lambda" {
  description = "Memória da Lambda (limitada no Academy)"
  type        = number
  default     = 512
  
  validation {
    condition     = var.memoria_lambda >= 128 && var.memoria_lambda <= 1024
    error_message = "No Academy, use memória entre 128MB e 1GB."
  }
}

variable "timeout_lambda" {
  description = "Timeout da Lambda (limitado no Academy)"
  type        = number
  default     = 30
  
  validation {
    condition     = var.timeout_lambda >= 3 && var.timeout_lambda <= 60
    error_message = "No Academy, timeout máximo é 60 segundos."
  }
}
```

### **FASE 2: CRUD Produtos**
**Duração estimada:** 2-3 dias
**Objetivo:** Validar padrões de desenvolvimento serverless
**Status:** ✅ CONCLUÍDA (com dados mock)

#### Estrutura
```
lambda-produtos/
├── src/main/java/br/com/lanchonete/produtos/
│   ├── ProdutosHandler.java
│   ├── domain/
│   │   ├── entities/Produto.java
│   │   ├── enums/CategoriaProduto.java
│   │   └── services/ServicoProduto.java
│   ├── application/
│   │   ├── usecases/
│   │   │   ├── CriarProduto.java
│   │   │   ├── ListarProdutos.java
│   │   │   ├── BuscarPorCategoria.java
│   │   │   └── AtualizarProduto.java
│   │   └── gateways/ProdutoGateway.java
│   └── adapters/
│       ├── mock/ProdutoMockGateway.java
│       └── rds/ProdutoRdsGateway.java (futuro)
```

#### APIs
```
GET    /produtos                           # Listar todos
GET    /produtos/{id}                      # Buscar por ID
GET    /produtos/categoria/{categoria}     # Por categoria
POST   /produtos                           # Criar produto
PUT    /produtos/{id}                      # Atualizar produto
DELETE /produtos/{id}                      # Remover produto
```

#### Mock de Produtos
```java
// Produtos pré-cadastrados para teste
PRODUTOS_MOCK = Arrays.asList(
    new Produto(1L, "Big Mac", CategoriaProduto.LANCHE, new BigDecimal("25.90"), "Hambúrguer clássico"),
    new Produto(2L, "Batata Frita", CategoriaProduto.ACOMPANHAMENTO, new BigDecimal("12.50"), "Batata crocante"),
    new Produto(3L, "Coca-Cola", CategoriaProduto.BEBIDA, new BigDecimal("8.90"), "Refrigerante 350ml")
);
```

### **FASE 3: RDS Setup (Banco Gerenciável - PDF)**
**Duração estimada:** 1 dia
**Objetivo:** Atender requisito de "banco de dados gerenciável" do PDF
**Base:** Item 5 do PDF - "banco de dados gerenciáveis (RDS)"
**Status:** ✅ CONCLUÍDA (RDS MySQL funcionando)

#### Infraestrutura RDS para AWS Academy
```hcl
# terraform/rds.tf
resource "aws_db_instance" "lanchonete_db" {
  identifier = "lanchonete-${var.environment}"
  engine     = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"  # Free tier - obrigatório no Academy
  
  allocated_storage = 20
  storage_type     = "gp2"
  storage_encrypted = true
  
  db_name  = "lanchonete"
  username = "admin"
  password = var.db_password
  
  # Configuração simplificada para Academy (sem VPC complexa)
  publicly_accessible = false
  skip_final_snapshot = true
  deletion_protection = false
  
  # Configurações de backup conservadoras
  backup_retention_period = 1  # Mínimo para Academy
  backup_window = "03:00-04:00"
  maintenance_window = "sun:04:00-sun:05:00"
  
  tags = merge(var.default_tags, {
    Name = "lanchonete-${var.environment}-db"
  })
}

# Security Group simplificado para Academy
resource "aws_security_group" "rds" {
  name_prefix = "lanchonete-rds-"
  description = "Security group para RDS MySQL"
  
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Simplificado para Academy (não recomendado para produção)
  }
  
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
```

#### Considerações especiais para Academy
- ✅ Usar `db.t3.micro` (Free Tier)
- ✅ Storage mínimo (20GB)
- ✅ Backup retention mínimo (1 dia)
- ✅ Security Group simplificado
- ⚠️ Sem VPC complexa inicialmente
- ⚠️ Credenciais em variáveis (não Secrets Manager para simplificar)

#### Scripts de Migração
```sql
-- migration/001_criar_tabelas.sql
CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produtos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    categoria ENUM('LANCHE', 'ACOMPANHAMENTO', 'BEBIDA', 'SOBREMESA') NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserir dados iniciais
INSERT INTO clientes (cpf, nome, email) VALUES
('12345678901', 'João Silva', 'joao@email.com'),
('11144477735', 'Maria Santos', 'maria@email.com');

INSERT INTO produtos (nome, categoria, preco, descricao) VALUES
('Big Mac', 'LANCHE', 25.90, 'Hambúrguer clássico'),
('Batata Frita', 'ACOMPANHAMENTO', 12.50, 'Batata crocante'),
('Coca-Cola', 'BEBIDA', 8.90, 'Refrigerante 350ml');
```

### **FASE 3.1: Integração Lambda-RDS (Substituir Mocks)**
**Duração estimada:** 1-2 dias
**Objetivo:** Conectar Lambdas existentes ao RDS MySQL
**Base:** Manter funcionalidades atuais, trocar dados mock por dados reais

#### Subetapas:
- ✅ **lambda-auth-cpf → RDS:** Substituir ClienteMockGateway por ClienteRdsGateway (só leitura)
- 🔄 **lambda-produtos → RDS:** Substituir ProdutoMockGateway por ProdutoRdsGateway (CRUD completo)

#### Limitações identificadas:
⚠️ **PENDÊNCIA CRÍTICA:** Lambda-auth-cpf só implementa `buscarPorCpf`. Os métodos `salvar()` e `buscarPorId()` retornam `UnsupportedOperationException` pois não são usados na autenticação.

**SOLUÇÃO FUTURA:** Criar **FASE 3.2: Lambda CRUD Clientes** para implementar cadastro, edição e busca de clientes com CRUD completo.

### **FASE 3.2: Lambda CRUD Clientes (PENDENTE)**
**Duração estimada:** 2-3 dias  
**Objetivo:** Implementar CRUD completo de clientes
**Justificativa:** Atualmente lambda-auth-cpf só faz leitura. Sistema precisa de endpoints para cadastrar/editar clientes.

#### Estrutura planejada:
```
lambda-clientes/
├── POST /clientes         # Cadastrar cliente  
├── GET /clientes/{id}     # Buscar por ID
├── PUT /clientes/{id}     # Atualizar cliente
├── DELETE /clientes/{id}  # Remover cliente
└── GET /clientes/cpf/{cpf} # Buscar por CPF
```

#### Implementação pendente:
- ClienteRdsGateway com todos os métodos (salvar, buscar, atualizar, remover)
- Use cases: CriarCliente, EditarCliente, RemoverCliente, BuscarCliente
- Service layer + Handler + API Gateway
- Testes unitários e integração

### **FASE 4: Gestão de Pedidos (Core Business)**
**Duração estimada:** 3-4 dias
**Objetivo:** Migrar lógica core de negócio mantendo funcionalidades atuais
**Base:** Preservar todos os fluxos de pedido do sistema atual

#### Estrutura
```
lambda-pedidos/
├── src/main/java/br/com/lanchonete/pedidos/
│   ├── PedidosHandler.java
│   ├── domain/
│   │   ├── entities/
│   │   │   ├── Pedido.java
│   │   │   └── ItemPedido.java
│   │   ├── enums/
│   │   │   ├── StatusPedido.java
│   │   │   └── StatusPagamento.java
│   │   └── services/ServicoPedido.java
│   ├── application/
│   │   ├── usecases/
│   │   │   ├── RealizarPedido.java
│   │   │   ├── ListarPedidos.java
│   │   │   └── AtualizarStatusPedido.java
│   │   └── gateways/PedidoGateway.java
│   └── adapters/
│       └── rds/PedidoRdsGateway.java
```

#### APIs
```
POST   /pedidos                    # Criar pedido
GET    /pedidos                    # Listar pedidos
GET    /pedidos/{id}               # Buscar pedido
PUT    /pedidos/{id}/status        # Atualizar status
GET    /pedidos/cozinha            # Pedidos para cozinha
```

### **FASE 5: Sistema de Pagamento (Manter Integração)**
**Duração estimada:** 2-3 dias
**Objetivo:** Preservar integração com Mercado Pago em arquitetura serverless
**Base:** Manter webhook e processamento de pagamento atuais

#### Estrutura
```
lambda-pagamento/
├── src/main/java/br/com/lanchonete/pagamento/
│   ├── PagamentoHandler.java
│   ├── WebhookHandler.java
│   ├── domain/
│   │   ├── entities/Pagamento.java
│   │   └── services/ServicoPagamento.java
│   ├── application/
│   │   ├── usecases/
│   │   │   ├── ProcessarPagamento.java
│   │   │   └── ProcessarWebhook.java
│   │   └── gateways/PagamentoGateway.java
│   └── adapters/
│       ├── mercadopago/AdapterMercadoPago.java
│       └── rds/PagamentoRdsGateway.java
```

## 🛠️ Ferramentas e Dependências

### Maven Dependencies (todas as fases)
```xml
<dependencies>
    <!-- AWS Lambda -->
    <dependency>
        <groupId>com.amazonaws</groupId>
        <artifactId>aws-lambda-java-core</artifactId>
        <version>1.2.3</version>
    </dependency>
    <dependency>
        <groupId>com.amazonaws</groupId>
        <artifactId>aws-lambda-java-events</artifactId>
        <version>3.11.4</version>
    </dependency>
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.0</version>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- RDS/MySQL (fases 3+) -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.0.1</version>
    </dependency>
    
    <!-- Testes -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.7.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Terraform Modules para AWS Academy
```
terraform/
├── main.tf                    # Provider e LabRole
├── lambda.tf                  # Lambda functions
├── api-gateway.tf            # API Gateway
├── rds.tf                    # RDS MySQL (simplificado)
├── variables.tf              # Variáveis com validações Academy
├── outputs.tf                # Outputs importantes
├── terraform.tfvars          # Valores específicos
└── README.md                 # Instruções Academy

# Estrutura simplificada - sem modules complexos para Academy
# Todos os recursos em arquivos únicos para facilitar debug
```

#### Configuração base para Academy
```hcl
# main.tf
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = merge(var.default_tags, {
      Environment = "aws-academy"
      ManagedBy   = "terraform"
      CreatedAt   = formatdate("YYYY-MM-DD", timestamp())
    })
  }
}

# Dados da conta Academy
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# LabRole - obrigatória no Academy
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# Outputs informativos sobre Academy
output "academy_info" {
  description = "Informações do ambiente Academy"
  value = {
    account_id = data.aws_caller_identity.current.account_id
    region     = data.aws_region.current.name
    lab_role   = data.aws_iam_role.lab_role.arn
    using_academy = true
  }
}
```

## 🧪 Metodologia de Teste

### **⚠️ IMPORTANTE - DIVISÃO DE RESPONSABILIDADES**
- **Claude Code:** Desenvolve código e configura infraestrutura (Terraform)
- **Usuário:** Executa build, deploy e todos os testes de endpoints
- **Claude Code:** Fornece comandos exatos de build, deploy e teste
- **Usuário:** Executa comandos e reporta resultados para Claude Code continuar

### **🎯 Fluxo de Trabalho**
1. **Claude Code** desenvolve código e configura infraestrutura
2. **Claude Code** fornece comandos específicos de build e deploy
3. **Usuário** executa build e deploy dos recursos AWS
4. **Claude Code** fornece comandos de teste específicos
5. **Usuário** executa os testes de endpoints e reporta resultados
6. **Claude Code** analisa resultados e ajusta código se necessário
7. **Repetir** até todos os testes passarem

### **📋 Comandos Por Fase**

#### **FASE 1 - Autenticação CPF**

**📦 Comandos de Build e Deploy (Claude Code fornece):**
```bash
# 1. Build da aplicação Maven
mvn clean package

# 2. Navegar para Terraform
cd terraform

# 3. Inicializar Terraform (se necessário)
terraform init

# 4. Planejar deploy
terraform plan

# 5. Aplicar recursos
terraform apply -auto-approve

# 6. Verificar outputs
terraform output
```

**🧪 Comandos de Teste (Claude Code fornece):**
```bash
# Pegar URL da API
API_URL=$(terraform output -raw api_gateway_url)
echo "API URL: $API_URL"

# Teste 1: Cliente Anônimo (CPF vazio)
curl -X POST "$API_URL" -H "Content-Type: application/json" -d '{"cpf": ""}'

# Teste 2: Cliente Anônimo (CPF null) 
curl -X POST "$API_URL" -H "Content-Type: application/json" -d '{"cpf": null}'

# Teste 3: Cliente Identificado
curl -X POST "$API_URL" -H "Content-Type: application/json" -d '{"cpf": "12345678901"}'

# Teste 4: Request inválido (sem CPF)
curl -X POST "$API_URL" -H "Content-Type: application/json" -d '{}'

# Teste 5: CPF não encontrado
curl -X POST "$API_URL" -H "Content-Type: application/json" -d '{"cpf": "99999999999"}'

# Teste 6: CPF formato inválido
curl -X POST "$API_URL" -H "Content-Type: application/json" -d '{"cpf": "123"}'
```

**Usuário executa e reporta:** ✅/❌ + output para cada teste

#### **FASE 2 - CRUD Produtos**  

**📦 Comandos de Build e Deploy (Claude Code fornece):**
```bash
# 1. Build da aplicação Maven
mvn clean package

# 2. Deploy Terraform
cd terraform
terraform plan
terraform apply -auto-approve
terraform output
```

**🧪 Comandos de Teste (Claude Code fornece):**
```bash
# Pegar URL da API
API_URL=$(terraform output -raw api_gateway_url)

# Teste 1: Listar todos os produtos
curl -X GET "$API_URL/produtos"

# Teste 2: Listar produtos por categoria
curl -X GET "$API_URL/produtos/categoria/LANCHE"
curl -X GET "$API_URL/produtos/categoria/BEBIDA"

# Teste 3: Buscar produto específico
curl -X GET "$API_URL/produtos/1"

# Teste 4: Produto não encontrado
curl -X GET "$API_URL/produtos/999"
```

**Usuário executa e reporta:** ✅/❌ + output para cada teste

#### **FASE 3 - RDS Setup**

**📦 Comandos de Build e Deploy (Claude Code fornece):**
```bash
# 1. Deploy RDS com Terraform  
cd terraform
terraform plan
terraform apply -auto-approve
terraform output
```

**🧪 Comandos de Teste (Claude Code fornece):**
```bash
# Verificar RDS criada
aws rds describe-db-instances --query 'DBInstances[?contains(DBInstanceIdentifier, `lanchonete`)].{ID:DBInstanceIdentifier,Status:DBInstanceStatus,Endpoint:Endpoint.Address}'

# Testar conexão (se necessário)
mysql -h [ENDPOINT] -u admin -p -e "SHOW DATABASES;"
```

**Usuário executa e reporta:** ✅/❌ + output

### **🔍 Validações Específicas**

#### **JWT Token Validation**
**Claude Code fornece:**
```bash
# Pegar um token válido do teste anterior e decodificar
echo "[TOKEN_PAYLOAD]" | base64 -d
```

**Usuário executa e valida:**
- Claims corretos (tipo, clienteId, cpf, nome, etc.)
- Expiração configurada (3600 segundos)
- Diferenças entre token anônimo vs identificado

#### **Logs e Monitoramento**
**Claude Code fornece:**
```bash
# Ver logs da Lambda em tempo real
LAMBDA_NAME=$(terraform output -raw lambda_function_name)
aws logs tail "/aws/lambda/$LAMBDA_NAME" --follow

# Verificar métricas
aws cloudwatch get-metric-statistics \
  --namespace AWS/Lambda \
  --metric-name Invocations \
  --dimensions Name=FunctionName,Value=$LAMBDA_NAME \
  --start-time 2024-08-20T10:00:00Z \
  --end-time 2024-08-20T11:00:00Z \
  --period 300 \
  --statistics Sum
```

**Usuário executa e reporta:** Logs de erro, latência, execuções

### **🚨 Troubleshooting**
**Se usuário reportar erro, Claude Code investiga:**
1. **Status HTTP diferente do esperado** → Verificar logs Lambda
2. **Response JSON malformado** → Verificar serialização 
3. **Timeout** → Verificar configuração memory/timeout
4. **Permissões** → Verificar IAM roles e policies
5. **Conectividade** → Verificar API Gateway integration

### **📊 Template de Report do Usuário**

**Para Build e Deploy:**
```
ETAPA: [Build/Deploy/Teste]
COMANDO: [Comando executado]
STATUS: ✅ SUCESSO / ❌ FALHA
OUTPUT: [Output completo ou resumo se muito longo]
OBSERVAÇÕES: [Qualquer erro, warning ou observação relevante]
```

**Para Testes de Endpoint:**
```
TESTE: [Nome do teste específico]
COMANDO: [Comando curl executado]
STATUS: ✅ SUCESSO / ❌ FALHA
STATUS_HTTP: [200, 400, 500, etc.]
RESPONSE: [JSON response completo]
OBSERVAÇÕES: [Tempo de resposta, erros, etc.]
```

### Estratégia de Testes Original
```bash
# Verificar ambiente Academy
aws sts get-caller-identity
aws iam get-role --role-name LabRole

# Fase 1 - Autenticação
API_URL=$(terraform output -raw api_gateway_url)
curl -X POST $API_URL/auth/cpf \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'

# Fase 2 - Produtos  
curl -X GET $API_URL/produtos
curl -X GET $API_URL/produtos/categoria/LANCHE

# Fase 4 - Pedidos
JWT_TOKEN="eyJhbGciOiJIUzI1NiIs..."  # Token da autenticação
curl -X POST $API_URL/pedidos \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"clienteId": 1, "itens": [{"produtoId": 1, "quantidade": 2}]}'
```

### Monitoramento no Academy
```bash
# Ver logs da Lambda
LAMBDA_NAME=$(terraform output -raw lambda_function_name)
aws logs tail "/aws/lambda/$LAMBDA_NAME" --follow

# Listar recursos criados
aws lambda list-functions --query 'Functions[?contains(FunctionName, `lanchonete`)].FunctionName'
aws apigateway get-rest-apis --query 'items[?contains(name, `lanchonete`)].name'
aws rds describe-db-instances --query 'DBInstances[?contains(DBInstanceIdentifier, `lanchonete`)].DBInstanceIdentifier'

# Verificar custos (sempre $0 no Academy)
aws ce get-cost-and-usage --time-period Start=2024-08-01,End=2024-08-31 --granularity MONTHLY --metrics BlendedCost
```

### Troubleshooting Academy
```bash
# Credenciais expiraram?
aws sts get-caller-identity  # Se falhar, renovar no Academy

# LabRole existe?
aws iam get-role --role-name LabRole

# Região disponível?
aws ec2 describe-regions --query 'Regions[?RegionName==`us-east-1`].RegionName'

# Lambda dentro dos limites?
aws lambda get-function --function-name $LAMBDA_NAME \
  --query 'Configuration.[MemorySize,Timeout]'
```

### Validação End-to-End
1. Autenticar com CPF
2. Listar produtos por categoria
3. Criar pedido
4. Simular webhook de pagamento
5. Verificar status atualizado

## 📝 Checklist de Entrega (Tech Challenge)

### **Requisitos Obrigatórios do PDF**
- [ ] **API Gateway** para receber solicitações externas ← FASE 1
- [ ] **Function serverless** para autenticar/consultar cliente por CPF ← FASE 1  
- [ ] **Identificação apenas por CPF** (sem senha) ← FASE 1
- [ ] **JWT ou equivalente** para fluxo de integração ← FASE 1
- [ ] **Banco de dados gerenciável** (RDS) ← FASE 3
- [ ] **Toda infraestrutura com Terraform** ← Todas as fases
- [ ] **Segregação em repositórios** conforme especificado
- [ ] **Branches protegidas** com Pull Request obrigatório
- [ ] **Secrets** para valores sensíveis
- [ ] **Deploy automatizado** com Actions
- [ ] **Documentação** da arquitetura desenvolvida
- [ ] **Vídeo demonstrativo** conforme especificações do PDF

### **Estrutura de Repositórios (Conforme PDF)**
```
Repositórios obrigatórios conforme Item 2 do PDF:
1. lambda-auth-cpf/          # ← "1 repositório para o Lambda"
2. terraform-rds/            # ← "1 repositório para infra banco de dados"  
3. terraform-k8s/ (opcional) # ← "1 repositório para infra Kubernetes" 
4. lanchonete-app/ (atual)   # ← "1 repositório para aplicação Kubernetes"

Estrutura sugerida para serverless:
1. lambda-auth-cpf/          # Autenticação CPF (obrigatório)
2. lambda-produtos/          # Gestão de produtos  
3. lambda-pedidos/           # Gestão de pedidos
4. lambda-pagamento/         # Sistema de pagamento
5. terraform-rds/            # Banco de dados RDS
6. terraform-shared/         # Infraestrutura compartilhada
```

### **Documentação Obrigatória (Item 4 do PDF)**
- [ ] **Modelagem de dados** seguindo padrões
- [ ] **Justificativa** da escolha do banco (MySQL → RDS)
- [ ] **Documentação** da estrutura melhorada
- [ ] **Explicação** de cada serviço/arquitetura escolhida

### **Vídeo Demonstrativo (Item 7 do PDF)**
- [ ] **Arquitetura desenvolvida** em cloud funcionando
- [ ] **Execução de Pipelines** CI/CD (checks verdes)
- [ ] **Serviços criados** na cloud em pleno funcionamento
- [ ] **Explicação** da função e escolha de cada serviço
- [ ] **Publicação** no YouTube/Vimeo ou Google Drive/OneDrive
- [ ] **Visibilidade** pública ou não listado

### **Entrega Final (Item 6 do PDF)**
- [ ] **Repositório privado** com acesso para usuário `soatarchitecture`
- [ ] **Todos os códigos** (Lambda, Terraform, CI/CD actions)
- [ ] **Requisitos cumpridos** conforme especificação
- [ ] **URL do vídeo** incluída na documentação

## 🚀 Próximos Passos

### **🎯 Fluxo de Desenvolvimento**
1. **Claude Code** implementa a fase (código + infraestrutura)
2. **Claude Code** fornece comandos de build e deploy
3. **Usuário** executa build e deploy na AWS
4. **Claude Code** fornece comandos de teste para o usuário
5. **Usuário** executa os testes e reporta resultados
6. **Claude Code** corrige problemas (se houver) e prossegue

### Para Começar (FASE 1) - AWS Academy
1. **Configurar ambiente Academy** (Usuário)
   ```bash
   # Iniciar lab e configurar credenciais
   aws configure set aws_access_key_id AKIA...
   aws configure set aws_secret_access_key wJal...
   aws configure set aws_session_token IQoJ...
   aws configure set region us-east-1
   ```

2. **Verificar LabRole**
   ```bash
   aws iam get-role --role-name LabRole
   ```

3. **Criar projeto `lambda-auth-cpf`**
4. **Implementar validação de CPF** (reutilizar código atual)
5. **Criar mock de clientes**
6. **Implementar geração de JWT**
7. **Configurar Terraform para API Gateway + Lambda** (usando LabRole)
8. **Deploy e teste na AWS**

### Comandos Iniciais
```bash
# Criar projeto
mkdir lambda-auth-cpf && cd lambda-auth-cpf

# Estrutura Maven
mvn archetype:generate \
  -DgroupId=br.com.lanchonete.auth \
  -DartifactId=lambda-auth-cpf \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

# Setup Terraform para Academy
mkdir terraform && cd terraform
terraform init

# Arquivo terraform.tfvars para Academy
cat > terraform.tfvars << EOF
aws_region      = "us-east-1"
environment     = "dev"
project_name    = "lanchonete"
lambda_memory   = 512
lambda_timeout  = 30
jwt_secret      = "academy-jwt-secret-123"
db_password     = "LanchoneteDB123!"

default_tags = {
  Project     = "tech-challenge-fase3"
  Environment = "aws-academy"
  Course      = "soat-pos-tech"
  ManagedBy   = "terraform"
}
EOF
```

### ⚠️ Lembretes Importantes para Academy
1. **Credenciais expiram** - Renovar a cada 4-12 horas
2. **Usar LabRole sempre** - Não criar IAM roles próprias
3. **Limites de recursos** - Memory ≤ 1GB, Timeout ≤ 60s
4. **Sempre fazer cleanup** - `terraform destroy` ao final
5. **Região limitada** - Geralmente us-east-1 ou us-west-2

### 🎓 Dicas Academy
- Mantenha outputs detalhados para debug
- Teste cada fase completamente antes de avançar
- Documente problemas encontrados para o relatório
- Faça screenshots do console AWS para comprovação
- Use nomes simples e descritivos para recursos

---

**🎯 Foco:** Atender **rigorosamente** às especificações do Tech Challenge Fase 3 (PDF), começando com FASE 1 (requisitos obrigatórios) e evoluindo para arquitetura serverless completa.

**📄 Referência:** Todas as decisões devem estar alinhadas com as especificações detalhadas no PDF do Tech Challenge Fase 3.

**📧 Entrega:** Seguir exatamente os critérios de entrega especificados no PDF para aprovação no Tech Challenge.

- nao precisa de comentarios explicativos nas classes
- depois de toda sessao eu uso terraform destroy, entao sempre precisarei de instrucoes precisas para deployar tudo