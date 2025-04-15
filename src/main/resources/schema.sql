CREATE TABLE IF NOT EXISTS cliente (
    id IDENTITY PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    nome VARCHAR NOT NULL,
    email VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS produto (
    id IDENTITY PRIMARY KEY,
    nome VARCHAR NOT NULL UNIQUE,
    descricao VARCHAR,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR NOT NULL
);


CREATE TABLE IF NOT EXISTS pedido (
    id IDENTITY PRIMARY KEY,
    cliente_id BIGINT,
    status VARCHAR NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE IF NOT EXISTS item_pedido (
    id IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    FOREIGN KEY (produto_id) REFERENCES produto(id)
);