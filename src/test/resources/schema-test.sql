DROP ALL OBJECTS;

DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente (
    id IDENTITY PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome VARCHAR NOT NULL,
    email VARCHAR NOT NULL
);

DROP TABLE IF EXISTS produto;

CREATE TABLE produto (
    id IDENTITY PRIMARY KEY,
    nome VARCHAR NOT NULL UNIQUE,
    descricao VARCHAR,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR NOT NULL
);

DROP TABLE IF EXISTS pedido;

CREATE TABLE pedido (
    id IDENTITY PRIMARY KEY,
    cliente_id BIGINT,
    status VARCHAR NOT NULL,
    status_pagamento VARCHAR NOT NULL DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

DROP TABLE IF EXISTS item_pedido;

CREATE TABLE item_pedido (
    id IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    FOREIGN KEY (produto_id) REFERENCES produto(id)
);