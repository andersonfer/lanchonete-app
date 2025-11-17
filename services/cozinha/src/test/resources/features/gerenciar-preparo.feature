# language: pt
Funcionalidade: Gerenciar Preparo de Pedidos
  Como um funcionário da cozinha
  Eu quero gerenciar o ciclo de vida dos pedidos
  Para controlar o fluxo de preparo na cozinha

  Cenário: Validar transições de status
    Dado que um pedido pode ter diferentes status
    Quando eu verifico os status possíveis
    Então o pedido deve ter os status: AGUARDANDO, EM_PREPARO, PRONTO, RETIRADO

  Cenário: Validar fluxo de preparo
    Dado que um pedido inicia com status AGUARDANDO
    Quando o pedido passa para EM_PREPARO
    E depois passa para PRONTO
    Então o fluxo de status deve estar correto
