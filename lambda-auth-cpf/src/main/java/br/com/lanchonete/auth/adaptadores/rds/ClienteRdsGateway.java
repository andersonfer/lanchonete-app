package br.com.lanchonete.auth.adaptadores.rds;

import br.com.lanchonete.auth.application.gateways.ClienteGateway;
import br.com.lanchonete.auth.domain.entities.Cliente;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClienteRdsGateway implements ClienteGateway {
    
    private final DataSource dataSource;
    
    public ClienteRdsGateway(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        String sql = "SELECT id, nome, email, cpf FROM cliente WHERE cpf = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, cpf);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearCliente(resultSet));
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por CPF no banco de dados", e);
        }
    }
    
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return Cliente.reconstituir(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("email"), 
                rs.getString("cpf")
        );
    }
}