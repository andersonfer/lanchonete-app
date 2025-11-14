DROP ALL OBJECTS;

CREATE TABLE produtos (
    id IDENTITY PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(500),
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(50) NOT NULL
);

CREATE TABLE pedidos (
    id IDENTITY PRIMARY KEY,
    numero_pedido VARCHAR(20) UNIQUE,
    cpf_cliente VARCHAR(11),
    cliente_nome VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE itens_pedido (
    id IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    produto_nome VARCHAR(255),
    quantidade INT NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
);
