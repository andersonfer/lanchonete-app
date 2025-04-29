-- Inserir produtos por categoria
-- LANCHES
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('X-Burger', 'Hambúrguer com queijo, alface e tomate', 18.90, 'LANCHE'),
('X-Bacon', 'Hambúrguer com bacon crocante e queijo', 22.90, 'LANCHE'),
('X-Tudo', 'Hambúrguer completo com bacon, ovo, queijo e salada', 25.90, 'LANCHE')
ON CONFLICT (nome) DO NOTHING;

-- ACOMPANHAMENTOS
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('Batata Frita P', 'Porção pequena de batata frita crocante', 10.90, 'ACOMPANHAMENTO'),
('Batata Frita G', 'Porção grande de batata frita crocante', 18.90, 'ACOMPANHAMENTO'),
('Onion Rings', 'Anéis de cebola empanados', 15.90, 'ACOMPANHAMENTO')
ON CONFLICT (nome) DO NOTHING;

-- BEBIDAS
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('Refrigerante Lata', 'Refrigerante em lata 350ml', 6.90, 'BEBIDA'),
('Suco Natural', 'Suco de fruta natural 400ml', 9.90, 'BEBIDA'),
('Água Mineral', 'Água mineral sem gás 500ml', 4.50, 'BEBIDA')
ON CONFLICT (nome) DO NOTHING;

-- SOBREMESAS
INSERT INTO produto (nome, descricao, preco, categoria) VALUES
('Pudim', 'Pudim de leite condensado', 8.90, 'SOBREMESA'),
('Sorvete', 'Duas bolas de sorvete com calda', 10.90, 'SOBREMESA'),
('Brownie', 'Brownie de chocolate com sorvete', 14.90, 'SOBREMESA')
ON CONFLICT (nome) DO NOTHING;