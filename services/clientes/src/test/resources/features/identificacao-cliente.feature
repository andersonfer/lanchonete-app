# language: pt
Funcionalidade: Identificação de Cliente
  Como um atendente da lanchonete
  Eu quero identificar clientes pelo CPF
  Para poder oferecer um atendimento personalizado

  Cenário: Identificação de cliente existente
    Dado que existe um cliente cadastrado com nome "Ana Costa", email "ana@email.com" e CPF "12345678901"
    Quando eu busco um cliente pelo CPF "12345678901"
    Então o sistema deve retornar os dados do cliente
    E o nome do cliente deve ser "Ana Costa"

  Cenário: Tentativa de identificação com CPF não cadastrado
    Dado que não existe um cliente com o CPF "99999999999"
    Quando eu busco um cliente pelo CPF "99999999999"
    Então o sistema deve retornar que o cliente não foi encontrado
