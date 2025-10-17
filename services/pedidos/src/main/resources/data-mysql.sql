-- Inserir produtos apenas se a tabela estiver vazia
INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'X-Burger' as nome, 'Hambúrguer com queijo' as descricao, 15.90 as preco, 'LANCHE' as categoria) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'X-Burger') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'X-Bacon', 'Hambúrguer com bacon', 18.90, 'LANCHE') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'X-Bacon') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'X-Salada', 'Hambúrguer com alface e tomate', 16.90, 'LANCHE') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'X-Salada') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'Batata Frita', 'Porção média', 10.00, 'ACOMPANHAMENTO') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'Batata Frita') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'Onion Rings', 'Porção de anéis de cebola', 12.00, 'ACOMPANHAMENTO') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'Onion Rings') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'Coca-Cola', 'Lata 350ml', 7.00, 'BEBIDA') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'Coca-Cola') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'Suco Natural', 'Copo 300ml', 8.00, 'BEBIDA') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'Suco Natural') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'Sorvete', 'Bola de sorvete', 6.00, 'SOBREMESA') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'Sorvete') LIMIT 1;

INSERT INTO produtos (nome, descricao, preco, categoria)
SELECT * FROM (SELECT 'Brownie', 'Brownie de chocolate', 9.00, 'SOBREMESA') AS tmp
WHERE NOT EXISTS (SELECT 1 FROM produtos WHERE nome = 'Brownie') LIMIT 1;
