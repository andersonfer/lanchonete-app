# Sistema de Autoatendimento para Lanchonete

Este é um projeto para o Tech Challenge da pós-graduação SOAT - fase 1.

## Descrição do Projeto

Sistema de autoatendimento para uma lanchonete de bairro, permitindo que os clientes façam pedidos sem interagir com um atendente.

## Tecnologias Utilizadas

- Java 17
- Spring Boot
- Spring JDBC
- H2 Database
- Lombok
- SpringDoc OpenAPI (Swagger)

## Estrutura do Projeto

O projeto segue os princípios da Arquitetura Hexagonal (Ports and Adapters):

- **Domínio**: Contém as entidades e regras de negócio
- **Aplicação**: Contém as portas e adaptadores
    - **Portas de Entrada**: Interfaces para os serviços da aplicação
    - **Portas de Saída**: Interfaces para recursos externos como banco de dados
    - **Adaptadores de Entrada**: Implementações das interfaces de entrada (REST Controllers)
    - **Adaptadores de Saída**: Implementações das interfaces de saída (Repositórios)

## Como Executar

### Pré-requisitos
- Java 17 ou superior
- Maven

### Passos
1. Clone o repositório
2. Execute `mvn spring-boot:run`
3. Acesse o Swagger: `http://localhost:8080/swagger-ui.html`
4. Acesse o console H2: `http://localhost:8080/h2-console`

## Funcionalidades Implementadas

- Cadastro de Cliente
- Identificação de Cliente por CPF
- Gerenciamento de Produtos
- Criação e Acompanhamento de Pedidos