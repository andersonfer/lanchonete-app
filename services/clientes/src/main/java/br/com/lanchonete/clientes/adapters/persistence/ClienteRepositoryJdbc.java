package br.com.lanchonete.clientes.adapters.persistence;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.model.Cliente;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class ClienteRepositoryJdbc implements ClienteGateway {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert inserter;
    private final RowMapper<Cliente> clienteRowMapper = (rs, rowNum) ->
            Cliente.reconstituir(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("cpf")
            );

    public ClienteRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.inserter = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("cliente")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Cliente salvar(final Cliente cliente) {
        if (cliente.getId() == null) {
            Map<String, Object> params = Map.of(
                    "nome", cliente.getNome(),
                    "email", cliente.getEmail().getValor(),
                    "cpf", cliente.getCpf().getValor()
            );
            Number novoId = inserter.executeAndReturnKey(params);
            return Cliente.reconstituir(
                    novoId.longValue(),
                    cliente.getNome(),
                    cliente.getEmail().getValor(),
                    cliente.getCpf().getValor()
            );
        }
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorCpf(final String cpf) {
        try {
            Cliente cliente = jdbcTemplate.queryForObject(
                    "SELECT * FROM cliente WHERE cpf = ?",
                    clienteRowMapper,
                    cpf
            );
            return Optional.ofNullable(cliente);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(final Long id) {
        try {
            Cliente cliente = jdbcTemplate.queryForObject(
                    "SELECT * FROM cliente WHERE id = ?",
                    clienteRowMapper,
                    id
            );
            return Optional.ofNullable(cliente);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
