package br.com.lanchonete.autoatendimento.gateways;

import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.Map;
import java.util.Optional;

public class ClienteGatewayJDBC implements ClienteGateway {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert inserter;
    private final RowMapper<Cliente> clienteRowMapper = (rs, rowNum) ->
            Cliente.reconstituir(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("cpf")
            );

    public ClienteGatewayJDBC(JdbcTemplate jdbcTemplate) {
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
            cliente.setId(novoId.longValue());

        }
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorCpf(final String cpf) {
        try {
            Cliente cliente = jdbcTemplate.queryForObject("SELECT * FROM cliente WHERE cpf = ?",
                    clienteRowMapper, cpf);
            return Optional.ofNullable(cliente);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(final Long id) {
        try {
            Cliente cliente = jdbcTemplate.queryForObject("SELECT * FROM cliente WHERE id = ?",
                    clienteRowMapper, id);
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
