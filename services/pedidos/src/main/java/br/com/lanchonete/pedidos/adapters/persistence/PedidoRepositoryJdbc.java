package br.com.lanchonete.pedidos.adapters.persistence;

import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PedidoRepositoryJdbc implements PedidoGateway {

    private final JdbcTemplate jdbcTemplate;

    public PedidoRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO pedidos (cpf_cliente, cliente_nome, status, data_criacao, valor_total) " +
                     "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, pedido.getCpfCliente());
            ps.setString(2, pedido.getClienteNome());
            ps.setString(3, pedido.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(pedido.getDataCriacao()));
            ps.setBigDecimal(5, pedido.getValorTotal());
            return ps;
        }, keyHolder);

        Long pedidoId = keyHolder.getKey().longValue();
        pedido.setId(pedidoId);

        // Salvar itens
        for (ItemPedido item : pedido.getItens()) {
            salvarItem(pedidoId, item);
        }

        return buscarPorId(pedidoId).orElse(pedido);
    }

    private void salvarItem(Long pedidoId, ItemPedido item) {
        String sql = "INSERT INTO itens_pedido (pedido_id, produto_id, produto_nome, quantidade, valor_unitario, valor_total) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                pedidoId,
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getValorTotal()
        );
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        String sql = "SELECT * FROM pedidos WHERE id = ?";

        List<Pedido> pedidos = jdbcTemplate.query(sql, new PedidoRowMapper(), id);

        if (pedidos.isEmpty()) {
            return Optional.empty();
        }

        Pedido pedido = pedidos.get(0);
        carregarItens(pedido);

        return Optional.of(pedido);
    }

    @Override
    public List<Pedido> listarTodos() {
        String sql = "SELECT * FROM pedidos ORDER BY data_criacao DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, new PedidoRowMapper());

        for (Pedido pedido : pedidos) {
            carregarItens(pedido);
        }

        return pedidos;
    }

    @Override
    public List<Pedido> listarPorStatus(StatusPedido status) {
        String sql = "SELECT * FROM pedidos WHERE status = ? ORDER BY data_criacao DESC";
        List<Pedido> pedidos = jdbcTemplate.query(sql, new PedidoRowMapper(), status.name());

        for (Pedido pedido : pedidos) {
            carregarItens(pedido);
        }

        return pedidos;
    }

    @Override
    @Transactional
    public void atualizarStatus(Long id, StatusPedido status) {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }

    private void carregarItens(Pedido pedido) {
        String sql = "SELECT * FROM itens_pedido WHERE pedido_id = ?";
        List<ItemPedido> itens = jdbcTemplate.query(sql, new ItemPedidoRowMapper(pedido), pedido.getId());
        pedido.setItens(itens);
        pedido.calcularValorTotal();
    }

    private static class PedidoRowMapper implements RowMapper<Pedido> {
        @Override
        public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String cpfCliente = rs.getString("cpf_cliente");
            String clienteNome = rs.getString("cliente_nome");
            StatusPedido status = StatusPedido.valueOf(rs.getString("status"));
            Timestamp dataCriacao = rs.getTimestamp("data_criacao");
            BigDecimal valorTotal = rs.getBigDecimal("valor_total");

            Pedido pedido = Pedido.criar(cpfCliente, status, dataCriacao.toLocalDateTime());
            pedido.setId(id);
            pedido.setClienteNome(clienteNome);
            pedido.setValorTotal(valorTotal);

            return pedido;
        }
    }

    private static class ItemPedidoRowMapper implements RowMapper<ItemPedido> {
        private final Pedido pedido;

        public ItemPedidoRowMapper(Pedido pedido) {
            this.pedido = pedido;
        }

        @Override
        public ItemPedido mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long itemId = rs.getLong("id");
            Long produtoId = rs.getLong("produto_id");
            String produtoNome = rs.getString("produto_nome");
            int quantidade = rs.getInt("quantidade");
            BigDecimal valorUnitario = rs.getBigDecimal("valor_unitario");
            BigDecimal valorTotal = rs.getBigDecimal("valor_total");

            // Reconstituir produto simplificado (apenas com dados necessários)
            Produto produto = Produto.reconstituir(produtoId, produtoNome, null, valorUnitario, null);

            return ItemPedido.reconstituir(itemId, pedido, produto, quantidade, valorUnitario, valorTotal);
        }
    }
}
