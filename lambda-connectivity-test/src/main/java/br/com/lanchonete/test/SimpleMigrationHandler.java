package br.com.lanchonete.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SimpleMigrationHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String dbUrl = System.getenv("DATABASE_URL");
            String dbName = System.getenv("DB_NAME");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");
            
            context.getLogger().log("Executando migrations simples...");
            
            String jdbcUrl = "jdbc:mysql://" + dbUrl + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
            
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement stmt = connection.createStatement()) {
                
                context.getLogger().log("Criando tabelas...");
                
                // Clientes
                stmt.execute("CREATE TABLE IF NOT EXISTS clientes (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "nome VARCHAR(255) NOT NULL," +
                    "email VARCHAR(255)," +
                    "cpf VARCHAR(11) UNIQUE NOT NULL," +
                    "INDEX idx_cpf (cpf)" +
                    ")");
                
                // Produtos
                stmt.execute("CREATE TABLE IF NOT EXISTS produtos (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "nome VARCHAR(255) NOT NULL," +
                    "categoria ENUM('LANCHE','BEBIDA','ACOMPANHAMENTO','SOBREMESA') NOT NULL," +
                    "preco DECIMAL(10,2) NOT NULL," +
                    "descricao TEXT," +
                    "ativo BOOLEAN DEFAULT TRUE," +
                    "INDEX idx_categoria (categoria)" +
                    ")");
                
                // Pedidos
                stmt.execute("CREATE TABLE IF NOT EXISTS pedidos (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "cliente_id BIGINT NULL," +
                    "status ENUM('RECEBIDO','EM_PREPARACAO','PRONTO','FINALIZADO') DEFAULT 'RECEBIDO'," +
                    "status_pagamento ENUM('PENDENTE','APROVADO','REJEITADO') DEFAULT 'PENDENTE'," +
                    "valor_total DECIMAL(10,2) NOT NULL," +
                    "FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL," +
                    "INDEX idx_cliente (cliente_id)," +
                    "INDEX idx_status (status)," +
                    "INDEX idx_status_pagamento (status_pagamento)" +
                    ")");
                
                // Itens do pedido
                stmt.execute("CREATE TABLE IF NOT EXISTS itens_pedido (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "pedido_id BIGINT NOT NULL," +
                    "produto_id BIGINT NOT NULL," +
                    "quantidade INT NOT NULL," +
                    "preco_unitario DECIMAL(10,2) NOT NULL," +
                    "FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (produto_id) REFERENCES produtos(id)," +
                    "INDEX idx_pedido (pedido_id)," +
                    "INDEX idx_produto (produto_id)" +
                    ")");
                
                context.getLogger().log("Inserindo dados...");
                
                // Seeds - clientes
                stmt.execute("INSERT IGNORE INTO clientes (nome, email, cpf) VALUES " +
                    "('João Silva', 'joao@email.com', '12345678901')," +
                    "('Maria Santos', 'maria@email.com', '98765432100')," +
                    "('Pedro Oliveira', 'pedro@email.com', '11122233344')");
                
                // Seeds - produtos
                stmt.execute("INSERT IGNORE INTO produtos (nome, categoria, preco, descricao) VALUES " +
                    "('Big Burguer', 'LANCHE', 25.90, 'Hambúrguer artesanal com carne de 200g')," +
                    "('Cheese Burguer', 'LANCHE', 22.50, 'Hambúrguer com queijo cheddar')," +
                    "('Bacon Burguer', 'LANCHE', 27.90, 'Hambúrguer com bacon crocante')," +
                    "('Coca-Cola', 'BEBIDA', 6.00, 'Refrigerante Coca-Cola 350ml')," +
                    "('Suco Natural', 'BEBIDA', 8.50, 'Suco natural de laranja 400ml')," +
                    "('Batata Frita', 'ACOMPANHAMENTO', 8.90, 'Batata frita crocante')," +
                    "('Onion Rings', 'ACOMPANHAMENTO', 10.90, 'Anéis de cebola empanados')," +
                    "('Sorvete', 'SOBREMESA', 12.90, 'Sorvete artesanal 2 bolas')," +
                    "('Brownie', 'SOBREMESA', 14.90, 'Brownie com calda de chocolate')");
                
                context.getLogger().log("Migrations executadas com sucesso!");
                
                response.put("status", "success");
                response.put("message", "Migrations executadas com sucesso");
            }
            
        } catch (Exception e) {
            context.getLogger().log("Erro nas migrations: " + e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}