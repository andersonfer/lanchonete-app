package br.com.lanchonete.autoatendimento.adaptadores.persistencia;

import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
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
        pedidoParams.put("status_pagamento", pedido.getStatusPagamento().name());
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
        // 1. Buscar todos os pedidos excluindo FINALIZADO e com ordenação por status e data
        final List<Pedido> pedidos = jdbcTemplate.query(
                "SELECT * FROM pedido " +
                "WHERE status != 'FINALIZADO' " +
                "ORDER BY " +
                "CASE " +
                "  WHEN status = 'PRONTO' THEN 1 " +
                "  WHEN status = 'EM_PREPARACAO' THEN 2 " +
                "  WHEN status = 'RECEBIDO' THEN 3 " +
                "  ELSE 4 " +
                "END, " +
                "data_criacao ASC",
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
            throw new RecursoNaoEncontradoException("Pedido não encontrado com ID: " + pedidoId);
        }
    }

    @Transactional
    public void atualizarStatusPagamento(final Long pedidoId, final StatusPagamento novoStatusPagamento) {
        int linhasAfetadas = jdbcTemplate.update(
                "UPDATE pedido SET status_pagamento = ? WHERE id = ?",
                novoStatusPagamento.name(), pedidoId
        );

        if (linhasAfetadas == 0) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado com ID: " + pedidoId);
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
        pedido.setStatusPagamento(StatusPagamento.valueOf(rs.getString("status_pagamento")));

        return pedido;
    }

    private ItemPedido mapearItemPedido(final ResultSet rs, final Pedido pedido) throws SQLException {
        final Long produtoId = rs.getLong("produto_id");
        final Produto produto = produtoGateway.buscarPorId(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + produtoId));

        return ItemPedido.reconstituir(
                rs.getLong("id"),
                pedido,
                produto,
                rs.getInt("quantidade"),
                rs.getBigDecimal("valor_unitario"),
                rs.getBigDecimal("valor_total")
        );

    }
}