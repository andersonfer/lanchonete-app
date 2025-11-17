# language: pt
Funcionalidade: Consultar Fila da Cozinha
  Como um funcionário da cozinha
  Eu quero visualizar a fila de pedidos
  Para saber quais pedidos preciso preparar

  @semPedidos
  Cenário: Listar fila vazia
    Dado que não existem pedidos na fila
    Quando eu consulto a fila da cozinha
    Então devo receber uma lista vazia

  @comPedidos
  Cenário: Listar fila com pedidos
    Dado que existem pedidos na fila da cozinha
    Quando eu consulto a fila da cozinha
    Então devo receber a lista de pedidos
    E os pedidos devem conter status e identificação
