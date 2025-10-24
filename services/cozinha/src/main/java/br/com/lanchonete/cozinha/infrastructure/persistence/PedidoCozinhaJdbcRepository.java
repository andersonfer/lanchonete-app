package br.com.lanchonete.cozinha.infrastructure.persistence;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PedidoCozinhaJdbcRepository implements PedidoCozinhaRepository {

    private final JdbcTemplate jdbcTemplate;

    public PedidoCozinhaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PedidoCozinha> rowMapper = (rs, rowNum) -> {
        PedidoCozinha pedido = new PedidoCozinha();
        pedido.setId(rs.getLong("id"));
        pedido.setPedidoId(rs.getLong("pedido_id"));
        pedido.setStatus(StatusPedido.valueOf(rs.getString("status")));

        Timestamp dataInicio = rs.getTimestamp("data_inicio");
        if (dataInicio != null) {
            pedido.setDataInicio(dataInicio.toLocalDateTime());
        }

        Timestamp dataFim = rs.getTimestamp("data_fim");
        if (dataFim != null) {
            pedido.setDataFim(dataFim.toLocalDateTime());
        }

        return pedido;
    };

    @Override
    public PedidoCozinha save(PedidoCozinha pedidoCozinha) {
        if (pedidoCozinha.getId() == null) {
            return insert(pedidoCozinha);
        } else {
            return update(pedidoCozinha);
        }
    }

    private PedidoCozinha insert(PedidoCozinha pedidoCozinha) {
        String sql = "INSERT INTO pedido_cozinha (pedido_id, status, data_inicio, data_fim) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, pedidoCozinha.getPedidoId());
            ps.setString(2, pedidoCozinha.getStatus().name());
            ps.setTimestamp(3, pedidoCozinha.getDataInicio() != null ? Timestamp.valueOf(pedidoCozinha.getDataInicio()) : null);
            ps.setTimestamp(4, pedidoCozinha.getDataFim() != null ? Timestamp.valueOf(pedidoCozinha.getDataFim()) : null);
            return ps;
        }, keyHolder);

        pedidoCozinha.setId(keyHolder.getKey().longValue());
        return pedidoCozinha;
    }

    private PedidoCozinha update(PedidoCozinha pedidoCozinha) {
        String sql = "UPDATE pedido_cozinha SET pedido_id = ?, status = ?, data_inicio = ?, data_fim = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                pedidoCozinha.getPedidoId(),
                pedidoCozinha.getStatus().name(),
                pedidoCozinha.getDataInicio() != null ? Timestamp.valueOf(pedidoCozinha.getDataInicio()) : null,
                pedidoCozinha.getDataFim() != null ? Timestamp.valueOf(pedidoCozinha.getDataFim()) : null,
                pedidoCozinha.getId()
        );
        return pedidoCozinha;
    }

    @Override
    public Optional<PedidoCozinha> findById(Long id) {
        String sql = "SELECT * FROM pedido_cozinha WHERE id = ?";
        try {
            PedidoCozinha pedido = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(pedido);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PedidoCozinha> findByPedidoId(Long pedidoId) {
        String sql = "SELECT * FROM pedido_cozinha WHERE pedido_id = ?";
        try {
            PedidoCozinha pedido = jdbcTemplate.queryForObject(sql, rowMapper, pedidoId);
            return Optional.ofNullable(pedido);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PedidoCozinha> findAll() {
        String sql = "SELECT * FROM pedido_cozinha ORDER BY data_inicio ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<PedidoCozinha> findByStatus(StatusPedido status) {
        String sql = "SELECT * FROM pedido_cozinha WHERE status = ? ORDER BY data_inicio ASC";
        return jdbcTemplate.query(sql, rowMapper, status.name());
    }
}
