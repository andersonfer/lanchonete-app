package br.com.lanchonete.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ConnectivityTestHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String dbUrl = System.getenv("DATABASE_URL");
            String dbName = System.getenv("DB_NAME");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");
            
            context.getLogger().log("Testando conectividade com RDS MySQL...");
            context.getLogger().log("Database URL: " + dbUrl);
            
            String jdbcUrl = "jdbc:mysql://" + dbUrl + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
            
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                context.getLogger().log("Conexão estabelecida com sucesso!");
                
                // Verificar se é uma ação customizada
                String action = (String) input.get("action");
                String customSql = (String) input.get("sql");
                
                if ("execute-sql".equals(action) && customSql != null) {
                    // Executar SQL customizado
                    context.getLogger().log("Executando SQL customizado: " + customSql);
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute(customSql);
                        response.put("status", "success");
                        response.put("message", "SQL executado com sucesso");
                        context.getLogger().log("SQL customizado executado com sucesso");
                    }
                    
                } else {
                    // Comportamento padrão - contar registros
                    try (Statement stmt = connection.createStatement()) {
                        // Count clientes
                        int totalClientes = 0;
                        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM clientes")) {
                            if (rs.next()) {
                                totalClientes = rs.getInt("total");
                                context.getLogger().log("Total de clientes encontrados: " + totalClientes);
                            }
                        }
                        
                        // Count produtos
                        int totalProdutos = 0;
                        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM produtos")) {
                            if (rs.next()) {
                                totalProdutos = rs.getInt("total");
                                context.getLogger().log("Total de produtos encontrados: " + totalProdutos);
                            }
                        }
                        
                        response.put("status", "success");
                        response.put("message", "Conectividade OK");
                        response.put("totalClientes", totalClientes);
                        response.put("totalProdutos", totalProdutos);
                    }
                }
            }
            
        } catch (Exception e) {
            context.getLogger().log("Erro na conectividade: " + e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}