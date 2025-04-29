CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(50) NOT NULL
);


CREATE TABLE IF NOT EXISTS pedido (
    id SERIAL PRIMARY KEY,
    cliente_id BIGINT,
    status VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE TABLE IF NOT EXISTS item_pedido (
    id SERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    FOREIGN KEY (produto_id) REFERENCES produto(id)
);