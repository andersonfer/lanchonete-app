# language: pt
Funcionalidade: Criar Pedido
  Como um cliente da lanchonete
  Eu quero criar um pedido
  Para que eu possa fazer meu pedido e pagar

  Cenário: Validar produtos disponíveis
    Dado que existem produtos cadastrados
    Quando eu consulto os produtos disponíveis
    Então devo ver produtos cadastrados no sistema

  Cenário: Criar pedido sem itens deve falhar
    Quando eu tento criar um pedido sem itens
    Então o sistema deve retornar um erro de validação

  Cenário: Validar estrutura de pedido
    Dado que existem produtos cadastrados
    Quando eu verifico a estrutura necessária para criar um pedido
    Então o pedido deve ter estrutura com itens e cada item com produto e quantidade
