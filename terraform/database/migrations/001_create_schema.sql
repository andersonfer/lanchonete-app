-- Schema principal do sistema de lanchonete

CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    cpf VARCHAR(11) UNIQUE NOT NULL,
    INDEX idx_cpf (cpf)
);

CREATE TABLE IF NOT EXISTS produtos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL UNIQUE,
    categoria ENUM('LANCHE','BEBIDA','ACOMPANHAMENTO','SOBREMESA') NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    INDEX idx_categoria (categoria)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id BIGINT NULL,
    status ENUM('RECEBIDO','EM_PREPARACAO','PRONTO','FINALIZADO') DEFAULT 'RECEBIDO',
    status_pagamento ENUM('PENDENTE','APROVADO','REJEITADO') DEFAULT 'PENDENTE',
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL,
    INDEX idx_cliente (cliente_id),
    INDEX idx_status (status),
    INDEX idx_status_pagamento (status_pagamento)
);

CREATE TABLE IF NOT EXISTS itens_pedido (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produtos(id),
    INDEX idx_pedido (pedido_id),
    INDEX idx_produto (produto_id)
);