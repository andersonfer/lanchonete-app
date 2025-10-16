CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL UNIQUE COMMENT 'CPF do cliente (apenas números)',
    nome VARCHAR(255) NOT NULL COMMENT 'Nome completo do cliente',
    email VARCHAR(255) NOT NULL COMMENT 'Email de contato'
) COMMENT='Cadastro de clientes';
