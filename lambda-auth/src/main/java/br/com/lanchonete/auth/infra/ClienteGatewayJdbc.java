package br.com.lanchonete.auth.infra;

import br.com.lanchonete.auth.gateway.ClienteGateway;
import br.com.lanchonete.auth.model.Cliente;
import java.sql.*;
import java.util.Optional;

public class ClienteGatewayJdbc implements ClienteGateway {
    
    private final String databaseUrl;
    private final String username;
    private final String password;
    
    public ClienteGatewayJdbc(String databaseUrl, String username, String password) {
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
    }
    
    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) throws Exception {
        String sql = "SELECT id, nome, email, cpf FROM clientes WHERE cpf = ?";
        
        try (Connection connection = DriverManager.getConnection(databaseUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, cpf);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Cliente cliente = new Cliente(
                        resultSet.getLong("id"),
                        resultSet.getString("nome"),
                        resultSet.getString("email"),
                        resultSet.getString("cpf")
                    );
                    return Optional.of(cliente);
                }
                return Optional.empty();
            }
        }
    }
}