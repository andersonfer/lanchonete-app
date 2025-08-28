-- =============================================================================
-- Tech Challenge Fase 3 - Dados iniciais para RDS MySQL
-- Script: 002_insert_data.sql
-- =============================================================================

-- CLIENTES DE TESTE (para lambda-auth-cpf)
INSERT INTO cliente (cpf, nome, email) VALUES
('12345678901', 'Maria Oliveira', 'maria.oliveira@email.com'),
('11144477735', 'João Silva', 'joao.silva@email.com'),
('98765432100', 'Pedro Costa', 'pedro.costa@email.com'),
('11111111111', 'Ana Santos', 'ana.santos@email.com'),
('22222222222', 'Carlos Ferreira', 'carlos.ferreira@email.com')
ON DUPLICATE KEY UPDATE
    nome = VALUES(nome),
    email = VALUES(email);

-- PRODUTOS (para lambda-produtos)
-- Inserir ou atualizar produtos (upsert baseado no nome único)
-- Usando sintaxe moderna do MySQL 8.0+ com alias

-- Lanches
INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('X-Burger', 'Hambúrguer com queijo, alface e tomate', 18.90, 'LANCHE') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

-- Acompanhamentos
INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Batata Frita P', 'Porção pequena de batata frita crocante', 10.90, 'ACOMPANHAMENTO') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Batata Frita G', 'Porção grande de batata frita crocante', 18.90, 'ACOMPANHAMENTO') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Onion Rings', 'Anéis de cebola empanados', 15.90, 'ACOMPANHAMENTO') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

-- Bebidas
INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Refrigerante Lata', 'Refrigerante em lata 350ml', 6.90, 'BEBIDA') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Suco Natural', 'Suco de fruta natural 400ml', 9.90, 'BEBIDA') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Água Mineral', 'Água mineral sem gás 500ml', 4.50, 'BEBIDA') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

-- Sobremesas
INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Pudim', 'Pudim de leite condensado', 8.90, 'SOBREMESA') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Sorvete', 'Duas bolas de sorvete com calda', 10.90, 'SOBREMESA') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;

INSERT INTO produto (nome, descricao, preco, categoria)
VALUES ('Brownie', 'Brownie de chocolate com sorvete', 14.90, 'SOBREMESA') AS novo
ON DUPLICATE KEY UPDATE
    descricao = novo.descricao,
    preco = novo.preco,
    categoria = novo.categoria;