package br.com.lanchonete.autoatendimento.adaptadores.persistencia;

import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.modelo.Cliente;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.Map;
import java.util.Optional;

public class ClienteRepositorioJDBC implements ClienteRepositorio {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert inserter;
    private final RowMapper<Cliente> clienteRowMapper = (rs, rowNum) ->
            Cliente.criarSemValidacao(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("cpf")
            );

    public ClienteRepositorioJDBC(JdbcTemplate jdbcTemplate) {
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
                    "email", cliente.getEmail(),
                    "cpf", cliente.getCpf()
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
