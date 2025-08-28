-- =============================================================================
-- DADOS INICIAIS - LANCHONETE
-- =============================================================================

-- Clientes de teste
INSERT INTO cliente (nome, email, cpf) VALUES
('João Silva', 'joao@email.com', '12345678901'),
('Maria Santos', 'maria@email.com', '98765432100'),
('Pedro Oliveira', 'pedro@email.com', '11122233344');

-- Lanches
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('X-Burger', 'Hambúrguer com queijo, alface e tomate', 18.90, 'LANCHE'),
('Big Burguer', 'Hambúrguer artesanal com carne de 200g', 25.90, 'LANCHE'),
('Bacon Burguer', 'Hambúrguer com bacon crocante', 27.90, 'LANCHE');

-- Acompanhamentos
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('Onion Rings', 'Anéis de cebola empanados', 15.90, 'ACOMPANHAMENTO'),
('Batata Frita', 'Batata frita crocante', 8.90, 'ACOMPANHAMENTO');

-- Bebidas
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('Refrigerante Lata', 'Refrigerante em lata 350ml', 6.90, 'BEBIDA'),
('Água Mineral', 'Água mineral sem gás 500ml', 4.50, 'BEBIDA'),
('Coca-Cola', 'Refrigerante Coca-Cola 350ml', 6.00, 'BEBIDA');

-- Sobremesas
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('Pudim', 'Pudim de leite condensado', 8.90, 'SOBREMESA'),
('Brownie', 'Brownie de chocolate com sorvete', 14.90, 'SOBREMESA');