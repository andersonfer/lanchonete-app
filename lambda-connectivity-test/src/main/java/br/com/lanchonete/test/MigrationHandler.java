package br.com.lanchonete.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class MigrationHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    
    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String dbUrl = System.getenv("DATABASE_URL");
            String dbName = System.getenv("DB_NAME");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");
            String s3Bucket = System.getenv("MIGRATIONS_BUCKET");
            
            context.getLogger().log("Executando migrations do S3 bucket: " + s3Bucket);
            
            String jdbcUrl = "jdbc:mysql://" + dbUrl + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
            
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                
                // Executar schema
                context.getLogger().log("Executando 001_create_schema.sql...");
                String schemaSql = readS3File(s3Bucket, "001_create_schema.sql", context);
                executeSqlStatements(connection, schemaSql, context);
                
                // Executar seeds
                context.getLogger().log("Executando 002_seed_data.sql...");
                String seedSql = readS3File(s3Bucket, "002_seed_data.sql", context);
                executeSqlStatements(connection, seedSql, context);
                
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
    
    private String readS3File(String bucket, String key, Context context) throws Exception {
        context.getLogger().log("Lendo arquivo S3: s3://" + bucket + "/" + key);
        
        S3Object s3Object = s3Client.getObject(bucket, key);
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    private void executeSqlStatements(Connection connection, String sql, Context context) throws Exception {
        // Remover comentários
        String cleanSql = sql.replaceAll("--.*?\n", "").trim();
        
        // Dividir por ; mas preservar statements completos
        String[] statements = cleanSql.split(";");
        
        try (Statement stmt = connection.createStatement()) {
            for (String statement : statements) {
                String cleanStatement = statement.trim();
                if (!cleanStatement.isEmpty()) {
                    context.getLogger().log("Executando: " + cleanStatement.substring(0, Math.min(80, cleanStatement.length())).replaceAll("\\s+", " ") + "...");
                    stmt.execute(cleanStatement);
                    context.getLogger().log("Statement executado com sucesso!");
                }
            }
        }
    }
}