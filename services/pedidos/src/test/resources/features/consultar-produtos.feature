# language: pt
Funcionalidade: Consultar Produtos
  Como um cliente da lanchonete
  Eu quero consultar os produtos disponíveis
  Para que eu possa escolher o que pedir

  Cenário: Listar todos os produtos
    Dado que existem produtos cadastrados no sistema
    Quando eu consulto todos os produtos
    Então devo receber uma lista com produtos
    E os produtos devem conter nome e preço
