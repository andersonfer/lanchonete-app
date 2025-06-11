package br.com.lanchonete.autoatendimento.gateways;

import br.com.lanchonete.autoatendimento.adaptadores.shared.excecao.RegistroNaoEncontradoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import br.com.lanchonete.autoatendimento.entidades.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class PedidoGatewayJDBC implements PedidoGateway {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert pedidoInserter;
    private final SimpleJdbcInsert itemPedidoInserter;
    private final ClienteGateway clienteGateway;
    private final ProdutoGateway produtoGateway;

    public PedidoGatewayJDBC(JdbcTemplate jdbcTemplate,
                             ClienteGateway clienteGateway,
                             ProdutoGateway produtoGateway) {
        this.jdbcTemplate = jdbcTemplate;
        this.clienteGateway = clienteGateway;
        this.produtoGateway = produtoGateway;

        this.pedidoInserter = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("pedido")
                .usingGeneratedKeyColumns("id");

        this.itemPedidoInserter = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("item_pedido")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    @Transactional
    public Pedido salvar(final Pedido pedido) {
        // 1. Salvar pedido
        final Map<String, Object> pedidoParams = new HashMap<>();
        pedidoParams.put("cliente_id", pedido.getCliente() != null ? pedido.getCliente().getId() : null);
        pedidoParams.put("status", pedido.getStatus().name());
        pedidoParams.put("data_criacao", Timestamp.valueOf(pedido.getDataCriacao()));
        pedidoParams.put("valor_total", pedido.getValorTotal());

        final Number pedidoId = pedidoInserter.executeAndReturnKey(pedidoParams);
        pedido.setId(pedidoId.longValue());

        // 2. Salvar itens do pedido
        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                Map<String, Object> itemParams = new HashMap<>();
                itemParams.put("pedido_id", pedido.getId());
                itemParams.put("produto_id", item.getProduto().getId());
                itemParams.put("quantidade", item.getQuantidade());
                itemParams.put("valor_unitario", item.getValorUnitario());
                itemParams.put("valor_total", item.getValorTotal());

                Number itemId = itemPedidoInserter.executeAndReturnKey(itemParams);
                item.setId(itemId.longValue());
            }
        }

        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(final Long id) {
        try {
            // 1. Buscar pedido
            Pedido pedido = jdbcTemplate.queryForObject(
                    "SELECT * FROM pedido WHERE id = ?",
                    (rs, rowNum) -> mapearPedido(rs),
                    id
            );

            // 2. Buscar itens do pedido
            if (pedido != null) {
                List<ItemPedido> itens = jdbcTemplate.query(
                        "SELECT * FROM item_pedido WHERE pedido_id = ?",
                        (rs, rowNum) -> mapearItemPedido(rs, pedido),
                        id
                );
                pedido.setItens(itens);
            }

            return Optional.ofNullable(pedido);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Pedido> listarTodos() {
        // 1. Buscar todos os pedidos
        final List<Pedido> pedidos = jdbcTemplate.query(
                "SELECT * FROM pedido ORDER BY data_criacao DESC",
                (rs, rowNum) -> mapearPedido(rs)
        );

        // 2. Para cada pedido, buscar seus itens
        for (final Pedido pedido : pedidos) {
            List<ItemPedido> itens = jdbcTemplate.query(
                    "SELECT * FROM item_pedido WHERE pedido_id = ?",
                    (rs, rowNum) -> mapearItemPedido(rs, pedido),
                    pedido.getId()
            );
            pedido.setItens(itens);
        }

        return pedidos;
    }

    @Override
    @Transactional
    public void atualizarStatus(final Long pedidoId, final StatusPedido novoStatus) {
        int linhasAfetadas = jdbcTemplate.update(
                "UPDATE pedido SET status = ? WHERE id = ?",
                novoStatus.name(), pedidoId
        );

        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Pedido", pedidoId);
        }
    }

    private Pedido mapearPedido(final ResultSet rs) throws SQLException {
        final Long clienteId = rs.getLong("cliente_id");
        Cliente cliente = null;

        // Se existir cliente_id, buscar o cliente
        if (!rs.wasNull()) {
            cliente = clienteGateway.buscarPorId(clienteId)
                    .orElse(null);
        }

        Pedido pedido = Pedido.criar(
                cliente,
                StatusPedido.valueOf(rs.getString("status")),
                rs.getTimestamp("data_criacao").toLocalDateTime()
        );

        pedido.setId(rs.getLong("id"));
        pedido.setValorTotal(rs.getBigDecimal("valor_total"));

        return pedido;
    }

    private ItemPedido mapearItemPedido(final ResultSet rs, final Pedido pedido) throws SQLException {
        final Long produtoId = rs.getLong("produto_id");
        final Produto produto = produtoGateway.buscarPorId(produtoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Produto", produtoId));

        return new ItemPedido(
                rs.getLong("id"),
                pedido,
                produto,
                rs.getInt("quantidade"),
                rs.getBigDecimal("valor_unitario"),
                rs.getBigDecimal("valor_total")
        );

    }
}