-- Dados iniciais para testes (idempotente)

INSERT INTO clientes (nome, email, cpf) VALUES
('João Silva', 'joao@email.com', '12345678901'),
('Maria Santos', 'maria@email.com', '98765432100'),
('Pedro Oliveira', 'pedro@email.com', '11122233344')
ON DUPLICATE KEY UPDATE 
nome = VALUES(nome),
email = VALUES(email);

INSERT INTO produtos (nome, categoria, preco, descricao) VALUES
('Big Burguer', 'LANCHE', 25.90, 'Hambúrguer artesanal com carne de 200g'),
('Cheese Burguer', 'LANCHE', 22.50, 'Hambúrguer com queijo cheddar'),
('Bacon Burguer', 'LANCHE', 27.90, 'Hambúrguer com bacon crocante'),
('Coca-Cola', 'BEBIDA', 6.00, 'Refrigerante Coca-Cola 350ml'),
('Suco Natural', 'BEBIDA', 8.50, 'Suco natural de laranja 400ml'),
('Batata Frita', 'ACOMPANHAMENTO', 8.90, 'Batata frita crocante'),
('Onion Rings', 'ACOMPANHAMENTO', 10.90, 'Anéis de cebola empanados'),
('Sorvete', 'SOBREMESA', 12.90, 'Sorvete artesanal 2 bolas'),
('Brownie', 'SOBREMESA', 14.90, 'Brownie com calda de chocolate')
ON DUPLICATE KEY UPDATE 
preco = VALUES(preco),
descricao = VALUES(descricao),
ativo = VALUES(ativo);