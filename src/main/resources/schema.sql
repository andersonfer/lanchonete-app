CREATE TABLE IF NOT EXISTS cliente (
    id IDENTITY PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    nome VARCHAR NOT NULL,
    email VARCHAR NOT NULL
);
