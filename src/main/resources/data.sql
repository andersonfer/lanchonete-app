-- LANCHES
MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('X-Burger', 'Hambúrguer com queijo, alface e tomate', 18.90, 'LANCHE');

-- ACOMPANHAMENTOS
MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Batata Frita P', 'Porção pequena de batata frita crocante', 10.90, 'ACOMPANHAMENTO');

MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Batata Frita G', 'Porção grande de batata frita crocante', 18.90, 'ACOMPANHAMENTO');

MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Onion Rings', 'Anéis de cebola empanados', 15.90, 'ACOMPANHAMENTO');

-- BEBIDAS
MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Refrigerante Lata', 'Refrigerante em lata 350ml', 6.90, 'BEBIDA');

MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Suco Natural', 'Suco de fruta natural 400ml', 9.90, 'BEBIDA');

MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Água Mineral', 'Água mineral sem gás 500ml', 4.50, 'BEBIDA');

-- SOBREMESAS
MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Pudim', 'Pudim de leite condensado', 8.90, 'SOBREMESA');

MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Sorvete', 'Duas bolas de sorvete com calda', 10.90, 'SOBREMESA');

MERGE INTO produto (nome, descricao, preco, categoria) KEY(nome)
VALUES('Brownie', 'Brownie de chocolate com sorvete', 14.90, 'SOBREMESA');