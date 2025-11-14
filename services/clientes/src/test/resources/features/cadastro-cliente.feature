# language: pt
Funcionalidade: Cadastro de Cliente
  Como um atendente da lanchonete
  Eu quero cadastrar clientes com CPF
  Para poder identificá-los e oferecer benefícios

  Cenário: Cadastro de cliente com CPF válido
    Dado que não existe um cliente com o CPF "12345678901"
    Quando eu cadastro um cliente com nome "João Silva", email "joao@email.com" e CPF "12345678901"
    Então o cliente deve ser cadastrado com sucesso
    E o cliente deve ter o nome "João Silva"
    E o cliente deve ter o CPF "12345678901"

  Cenário: Tentativa de cadastro com CPF duplicado
    Dado que existe um cliente com nome "Cliente Existente", email "existente@email.com" e CPF "98765432100"
    Quando eu tento cadastrar um cliente com nome "Maria Santos", email "maria@email.com" e CPF "98765432100"
    Então o sistema deve retornar um erro de CPF duplicado

  Cenário: Tentativa de cadastro com CPF inválido
    Quando eu tento cadastrar um cliente com nome "Pedro Oliveira", email "pedro@email.com" e CPF "111"
    Então o sistema deve retornar um erro de CPF inválido

  Cenário: Tentativa de cadastro com nome vazio
    Quando eu tento cadastrar um cliente com nome "", email "teste@email.com" e CPF "11122233344"
    Então o sistema deve retornar um erro de validação
