# language: pt
Funcionalidade: Validação de Valor de Pagamento
  Como um sistema de pagamento
  Eu quero validar os valores de pagamento
  Para garantir que apenas valores válidos sejam aceitos

  Cenário: Criar valor válido
    Quando eu crio um valor de R$ 50.00
    Então o valor deve ser criado com sucesso

  Cenário: Criar valor mínimo válido
    Quando eu crio um valor de R$ 0.01
    Então o valor deve ser criado com sucesso

  Cenário: Rejeitar valor negativo
    Quando eu tento criar um valor de R$ -10.00
    Então o sistema deve lançar exceção de valor inválido

  Cenário: Rejeitar valor zero
    Quando eu tento criar um valor de R$ 0.00
    Então o sistema deve lançar exceção de valor inválido

  Cenário: Rejeitar valor nulo
    Quando eu tento criar um valor nulo
    Então o sistema deve lançar exceção de valor nulo
