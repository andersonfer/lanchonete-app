CREATE TABLE IF NOT EXISTS pedido_cozinha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NULL,
    UNIQUE KEY uk_pedido_id (pedido_id)
);
