package br.com.lanchonete.pedidos.adapters.persistence;

import br.com.lanchonete.pedidos.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class PedidoRepositoryJdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PedidoRepositoryJdbc pedidoRepositorio;
    private ProdutoRepositoryJdbc produtoRepositorio;

    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void configurar() {
        pedidoRepositorio = new PedidoRepositoryJdbc(jdbcTemplate);
        produtoRepositorio = new ProdutoRepositoryJdbc(jdbcTemplate);

        // Inserir produtos diretamente no banco para os testes
        jdbcTemplate.update(
                "INSERT INTO produtos (id, nome, descricao, preco, categoria) VALUES (?, ?, ?, ?, ?)",
                1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), "LANCHE"
        );

        jdbcTemplate.update(
                "INSERT INTO produtos (id, nome, descricao, preco, categoria) VALUES (?, ?, ?, ?, ?)",
                2L, "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), "BEBIDA"
        );

        produto1 = Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE);
        produto2 = Produto.reconstituir(2L, "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), Categoria.BEBIDA);
    }

    @Test
    @DisplayName("Deve salvar pedido com itens")
    void t1() {
        Pedido pedido = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido.adicionarItem(ItemPedido.criar(produto1, 2));
        pedido.adicionarItem(ItemPedido.criar(produto2, 1));

        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        assertNotNull(pedidoSalvo.getId());
        assertEquals("12345678901", pedidoSalvo.getCpfCliente());
        assertEquals(StatusPedido.CRIADO, pedidoSalvo.getStatus());
        assertEquals(2, pedidoSalvo.getItens().size());
        assertEquals(new BigDecimal("35.00"), pedidoSalvo.getValorTotal());
    }

    @Test
    @DisplayName("Deve buscar pedido por ID")
    void t2() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        pedido.adicionarItem(ItemPedido.criar(produto1, 1));
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        Optional<Pedido> resultado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        assertTrue(resultado.isPresent());
        Pedido encontrado = resultado.get();
        assertEquals(pedidoSalvo.getId(), encontrado.getId());
        assertNull(encontrado.getCpfCliente());
        assertEquals(1, encontrado.getItens().size());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar ID inexistente")
    void t3() {
        Optional<Pedido> resultado = pedidoRepositorio.buscarPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void t4() {
        Pedido pedido1 = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido1.adicionarItem(ItemPedido.criar(produto1, 1));

        Pedido pedido2 = Pedido.criar(null, StatusPedido.REALIZADO, LocalDateTime.now());
        pedido2.adicionarItem(ItemPedido.criar(produto2, 2));

        pedidoRepositorio.salvar(pedido1);
        pedidoRepositorio.salvar(pedido2);

        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        assertEquals(2, pedidos.size());
    }

    @Test
    @DisplayName("Deve listar pedidos por status")
    void t5() {
        Pedido pedido1 = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido1.adicionarItem(ItemPedido.criar(produto1, 1));

        Pedido pedido2 = Pedido.criar(null, StatusPedido.REALIZADO, LocalDateTime.now());
        pedido2.adicionarItem(ItemPedido.criar(produto2, 2));

        pedidoRepositorio.salvar(pedido1);
        pedidoRepositorio.salvar(pedido2);

        List<Pedido> pedidosCriados = pedidoRepositorio.listarPorStatus(StatusPedido.CRIADO);
        List<Pedido> pedidosRealizados = pedidoRepositorio.listarPorStatus(StatusPedido.REALIZADO);

        assertEquals(1, pedidosCriados.size());
        assertEquals(StatusPedido.CRIADO, pedidosCriados.get(0).getStatus());
        assertEquals(1, pedidosRealizados.size());
        assertEquals(StatusPedido.REALIZADO, pedidosRealizados.get(0).getStatus());
    }

    @Test
    @DisplayName("Deve atualizar status do pedido")
    void t6() {
        Pedido pedido = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido.adicionarItem(ItemPedido.criar(produto1, 1));
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        pedidoRepositorio.atualizarStatus(pedidoSalvo.getId(), StatusPedido.PRONTO);

        Optional<Pedido> resultado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());
        assertTrue(resultado.isPresent());
        assertEquals(StatusPedido.PRONTO, resultado.get().getStatus());
    }

    @Test
    @DisplayName("Deve calcular valor total ao carregar itens")
    void t7() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        pedido.adicionarItem(ItemPedido.criar(produto1, 2));
        pedido.adicionarItem(ItemPedido.criar(produto2, 3));
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        Optional<Pedido> resultado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        assertTrue(resultado.isPresent());
        assertEquals(new BigDecimal("45.00"), resultado.get().getValorTotal());
    }

    @Test
    @DisplayName("Deve salvar pedido com cliente nome")
    void t8() {
        Pedido pedido = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido.setClienteNome("João Silva");
        pedido.adicionarItem(ItemPedido.criar(produto1, 1));

        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        Optional<Pedido> resultado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());
        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getClienteNome());
    }
}
