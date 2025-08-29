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
                
                // Testar uma query simples
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM clientes");
                    if (rs.next()) {
                        int totalClientes = rs.getInt("total");
                        context.getLogger().log("Total de clientes encontrados: " + totalClientes);
                        
                        response.put("status", "success");
                        response.put("message", "Conectividade OK");
                        response.put("totalClientes", totalClientes);
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