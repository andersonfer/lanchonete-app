DROP ALL OBJECTS;

DROP TABLE IF EXISTS pedido_cozinha;

CREATE TABLE pedido_cozinha (
    id IDENTITY PRIMARY KEY,
    pedido_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NULL
);
