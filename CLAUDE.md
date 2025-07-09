# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Commands

### Autoatendimento Project
```bash
# Navegar para o projeto
cd autoatendimento/

# Compilar o projeto
mvn compile

# Executar testes
mvn test

# Executar aplicação
mvn spring-boot:run

# Gerar package
mvn package

# Limpar e recompilar
mvn clean compile

# Executar todos os testes incluindo E2E
mvn test -Dtest="**/*Test"

# Executar com profile de desenvolvimento
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Executar com profile de produção
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Pagamento Project
```bash
# Navegar para o projeto
cd pagamento/

# Compilar o projeto
mvn compile

# Executar aplicação
mvn spring-boot:run

# Executar testes
mvn test
```

### Docker Compose
```bash
# Executar todos os serviços (MySQL + Autoatendimento + Pagamento)
docker-compose up

# Executar em background
docker-compose up -d

# Parar os serviços
docker-compose down

# Reconstruir e executar
docker-compose up --build

# Ver logs dos serviços
docker-compose logs -f autoatendimento
docker-compose logs -f pagamento
```

## IMPORTANTE: Sempre execute os testes

**SEMPRE execute todos os testes antes de considerar uma implementação concluída:**

```bash
# No projeto autoatendimento
cd autoatendimento/
mvn test

# No projeto pagamento (quando existir)
cd pagamento/
mvn test
```

Todos os testes devem passar antes de finalizar qualquer tarefa. O projeto possui cobertura completa com testes unitários, de integração e E2E.

## IMPORTANTE: Mantenha este arquivo atualizado

**SEMPRE atualize este arquivo CLAUDE.md quando houver mudanças significativas:**

- Novos projetos ou módulos
- Alterações na arquitetura
- Novos comandos importantes
- Mudanças nos contextos de domínio
- Novas configurações ou dependências

Este arquivo é fundamental para manter a produtividade em futuras sessões de desenvolvimento.

## Project Architecture

Este é um sistema de autoatendimento para lanchonetes composto por dois projetos Spring Boot implementados em Java 17, seguindo os princípios de Clean Architecture e Domain-Driven Design.

### Projetos

#### 1. **autoatendimento** (Porta 8080)
Sistema principal de autoatendimento

#### 2. **pagamento** (Porta 8081)
Mock do sistema de pagamento Mercado Pago

### Estrutura Arquitetural

#### Camadas Principais (de dentro para fora):

1. **Domínio** (`dominio/`):
   - Contém as entidades de negócio e regras de domínio
   - Modelos: `Cliente`, `Pedido`, `ItemPedido`, `Produto`
   - Value Objects: `Cpf`, `Email`, `NumeroPedido`, `Preco`
   - Enums: `StatusPedido`, `StatusPagamento`, `Categoria`
   - Exceções de domínio

2. **Aplicação** (`aplicacao/`):
   - Casos de uso (Use Cases) organizados por contexto
   - Portas de saída (interfaces para gateways)
   - Orquestração das regras de negócio

3. **Adaptadores** (`adaptadores/`):
   - **Persistência**: Implementações JDBC dos gateways
   - **REST**: Controllers, DTOs, APIs e serviços web
   - Implementa as portas definidas na camada de aplicação

#### Arquitetura de Adaptadores REST:

**Responsabilidades por camada:**

1. **Interface xxxAPI** (`adaptadores/rest/api/`):
   - Define o contrato da API REST
   - Documentação OpenAPI/Swagger
   - Especifica endpoints, parâmetros e respostas
   - Não contém implementação

2. **Controller** (`adaptadores/rest/controllers/`):
   - Implementa as interfaces xxxAPI
   - Recebe requisições HTTP e converte para DTOs
   - Orquestra chamadas para Services
   - Trata respostas HTTP e status codes
   - Não contém lógica de negócio

3. **Service** (`adaptadores/rest/servicos/`):
   - Converte DTOs em objetos de domínio
   - Chama casos de uso (Use Cases)
   - Converte respostas de domínio para DTOs
   - Trata exceções de domínio para HTTP
   - Camada de tradução entre REST e domínio

4. **Infraestrutura** (`infra/`):
   - Configurações do Spring Boot
   - Configuração de banco de dados (H2/MySQL)
   - Configuração do Swagger

### Contextos de Domínio

- **Cliente**: Cadastro e identificação de clientes
- **Produto**: Gerenciamento de produtos por categoria
- **Pedido**: Realização, acompanhamento e atualização de pedidos
- **Webhook**: Processamento de notificações de pagamento
- **Cozinha**: Operações específicas da cozinha

### Banco de Dados

- **Desenvolvimento**: H2 (em memória)
- **Produção**: MySQL
- Scripts de schema e dados em `resources/`

### Testes

- **Unitários**: Para cada camada e componente
- **Integração**: Para gateways e configurações
- **E2E**: Testes de fluxo completo em `e2e/`

### Profiles

- `dev`: Desenvolvimento (H2, logs detalhados)
- `prod`: Produção (MySQL, configurações otimizadas)

## Important Notes

- O projeto usa Spring Data JDBC (não JPA)
- Swagger UI disponível em `/swagger-ui.html`
- Todas as operações seguem os princípios de Clean Architecture
- DTOs são usados para comunicação externa (REST)
- Gateways implementam as portas de saída do domínio

## Coding Standards

### DTOs
- **SEMPRE use records** para todos os DTOs
- Records são a forma preferida para objetos de transferência de dados

### Annotations
- **Evite usar annotations quando possível**
- Prefira validações programáticas ao invés de annotations de validação
- Use annotations apenas quando estritamente necessário (ex: @RestController, @Service)

### Testes
- **SEMPRE implemente testes para cada alteração de código**
- **Testes unitários**: Para cada classe/método implementado
- **Testes de integração**: Para fluxos entre camadas (quando aplicável)
- **Testes E2E**: Avalie se a mudança impacta fluxos completos do usuário (ex: checkout → pagamento → webhook)
- **Cobertura obrigatória**: Controllers, Services, Use Cases, Gateways
- **Execute os testes**: mvn test deve passar 100% antes de considerar a implementação concluída

#### Padrões de Nomenclatura e Estrutura:
- **Nomenclatura de métodos**: `t1()`, `t2()`, `t3()`, etc.
- **Descrições obrigatórias**: Sempre usar `@DisplayName("Deve [ação] quando [condição]")`
- **Estrutura**: Setup com `@BeforeEach` e método `configurar()`
- **Assertions**: Sempre com mensagens descritivas
- **Arquivos**: `ClasseTest.java` para unitários, `ClasseIntegracaoTest.java` para integração

#### Exemplos de Estrutura:
```java
@Test
@DisplayName("Deve processar pagamento com sucesso quando dados são válidos")
void t1() {
    PagamentoRequestDTO request = new PagamentoRequestDTO("123", new BigDecimal("25.50"));
    
    PagamentoResponseDTO response = pagamentoService.processarPagamento(request);
    
    assertEquals("123", response.pedidoId(), "ID do pedido deve estar correto");
    assertEquals("PENDENTE", response.status(), "Status deve ser PENDENTE");
}
```

#### Observações importantes:
- **Não usar comentários** Arrange, Act, Assert nos testes
- **Estrutura implícita**: Setup, execução e verificação separados por linhas em branco
- **Foco na legibilidade**: O código deve ser autoexplicativo