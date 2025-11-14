package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void configurar() {
        produto1 = Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE);
        produto2 = Produto.reconstituir(2L, "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), Categoria.BEBIDA);
    }

    @Test
    @DisplayName("Deve criar pedido anônimo")
    void t1() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        assertNotNull(pedido);
        assertNull(pedido.getCpfCliente());
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
        assertNotNull(pedido.getDataCriacao());
    }

    @Test
    @DisplayName("Deve criar pedido com CPF de cliente")
    void t2() {
        String cpf = "12345678901";
        Pedido pedido = Pedido.criar(cpf, StatusPedido.CRIADO, LocalDateTime.now());

        assertNotNull(pedido);
        assertEquals(cpf, pedido.getCpfCliente());
    }

    @Test
    @DisplayName("Deve adicionar item ao pedido")
    void t3() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produto1, 2);

        pedido.adicionarItem(item);

        assertEquals(1, pedido.getItens().size());
        assertEquals(item, pedido.getItens().get(0));
    }

    @Test
    @DisplayName("Deve calcular valor total ao adicionar item")
    void t4() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produto1, 2);

        pedido.adicionarItem(item);

        assertEquals(new BigDecimal("30.00"), pedido.getValorTotal());
    }

    @Test
    @DisplayName("Deve calcular valor total com múltiplos itens")
    void t5() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        ItemPedido item1 = ItemPedido.criar(produto1, 2);
        ItemPedido item2 = ItemPedido.criar(produto2, 3);

        pedido.adicionarItem(item1);
        pedido.adicionarItem(item2);

        assertEquals(new BigDecimal("45.00"), pedido.getValorTotal());
    }

    @Test
    @DisplayName("Deve gerar número do pedido ao definir ID")
    void t6() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        pedido.setId(123L);

        assertEquals(123L, pedido.getId());
        assertEquals("PED-000123", pedido.getNumeroPedido().getValor());
    }

    @Test
    @DisplayName("Deve permitir definir nome do cliente")
    void t7() {
        Pedido pedido = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());

        pedido.setClienteNome("João Silva");

        assertEquals("João Silva", pedido.getClienteNome());
    }

    @Test
    @DisplayName("Deve permitir alterar status do pedido")
    void t8() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        pedido.setStatus(StatusPedido.PRONTO);

        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
    }

    @Test
    @DisplayName("Deve validar pedido com itens")
    void t9() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produto1, 1);
        pedido.adicionarItem(item);

        assertDoesNotThrow(() -> pedido.validar());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar pedido sem itens")
    void t10() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pedido.validar();
        });

        assertEquals("Pedido deve conter pelo menos um item", exception.getMessage());
    }

    @Test
    @DisplayName("Deve calcular valor total como zero quando não há itens")
    void t11() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        pedido.calcularValorTotal();

        assertEquals(BigDecimal.ZERO, pedido.getValorTotal());
    }

    @Test
    @DisplayName("Deve permitir definir valor total manualmente")
    void t12() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        pedido.setValorTotal(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), pedido.getValorTotal());
    }

    @Test
    @DisplayName("Deve verificar igualdade entre pedidos com mesmos dados")
    void t13() {
        LocalDateTime data = LocalDateTime.of(2024, 1, 15, 10, 30);
        Pedido pedido1 = Pedido.criar("12345678901", StatusPedido.CRIADO, data);
        pedido1.setId(1L);

        Pedido pedido2 = Pedido.criar("12345678901", StatusPedido.CRIADO, data);
        pedido2.setId(1L);

        assertEquals(pedido1, pedido2);
    }

    @Test
    @DisplayName("Deve verificar desigualdade entre pedidos com IDs diferentes")
    void t14() {
        Pedido pedido1 = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido1.setId(1L);

        Pedido pedido2 = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido2.setId(2L);

        assertNotEquals(pedido1, pedido2);
    }

    @Test
    @DisplayName("Deve verificar equals com objeto nulo")
    void t15() {
        Pedido pedido = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido.setId(1L);

        assertNotEquals(null, pedido);
    }


    @Test
    @DisplayName("Deve gerar hashCode consistente para pedidos iguais")
    void t18() {
        LocalDateTime data = LocalDateTime.of(2024, 1, 15, 10, 30);
        Pedido pedido1 = Pedido.criar("12345678901", StatusPedido.CRIADO, data);
        pedido1.setId(1L);

        Pedido pedido2 = Pedido.criar("12345678901", StatusPedido.CRIADO, data);
        pedido2.setId(1L);

        assertEquals(pedido1.hashCode(), pedido2.hashCode());
    }

    @Test
    @DisplayName("Deve gerar toString com informações do pedido")
    void t19() {
        Pedido pedido = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido.setId(1L);
        pedido.setClienteNome("João Silva");

        String toString = pedido.toString();

        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("PED-000001"));
        assertTrue(toString.contains("12345678901"));
        assertTrue(toString.contains("João Silva"));
        assertTrue(toString.contains("CRIADO"));
    }

    @Test
    @DisplayName("Deve permitir alterar CPF do cliente")
    void t20() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        pedido.setCpfCliente("12345678901");

        assertEquals("12345678901", pedido.getCpfCliente());
    }

    @Test
    @DisplayName("Deve permitir alterar data de criação")
    void t21() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        LocalDateTime novaData = LocalDateTime.of(2024, 1, 15, 10, 30);

        pedido.setDataCriacao(novaData);

        assertEquals(novaData, pedido.getDataCriacao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar item nulo")
    void t22() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());

        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            pedido.adicionarItem(null);
        });

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Deve permitir definir lista de itens")
    void t23() {
        Pedido pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        ItemPedido item1 = ItemPedido.criar(produto1, 2);
        ItemPedido item2 = ItemPedido.criar(produto2, 1);

        java.util.List<ItemPedido> itens = java.util.Arrays.asList(item1, item2);
        pedido.setItens(itens);

        assertEquals(2, pedido.getItens().size());
    }
}
