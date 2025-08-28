package br.com.lanchonete.auth.adaptadores.rds;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceConfig {
    
    private static DataSource dataSource;
    
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DataSourceConfig.class) {
                if (dataSource == null) {
                    dataSource = criarDataSource();
                }
            }
        }
        return dataSource;
    }
    
    private static DataSource criarDataSource() {
        HikariConfig config = new HikariConfig();
        
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String database = System.getenv("DB_NAME");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=true&requireSSL=false&serverTimezone=UTC", 
                                     host, port, database);
        
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}