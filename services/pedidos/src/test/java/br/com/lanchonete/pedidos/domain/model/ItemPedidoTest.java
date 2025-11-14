package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    private Produto produto;

    @BeforeEach
    void configurar() {
        produto = Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve criar item de pedido com quantidade válida")
    void t1() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        assertNotNull(item);
        assertEquals(produto, item.getProduto());
        assertEquals(2, item.getQuantidade());
        assertEquals(new BigDecimal("15.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("30.00"), item.getValorTotal());
    }

    @Test
    @DisplayName("Deve calcular valor total corretamente")
    void t2() {
        ItemPedido item = ItemPedido.criar(produto, 3);

        assertEquals(new BigDecimal("45.00"), item.getValorTotal());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto é nulo")
    void t3() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ItemPedido.criar(null, 1);
        });

        assertEquals("Produto é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade é zero")
    void t4() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ItemPedido.criar(produto, 0);
        });

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade é negativa")
    void t5() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ItemPedido.criar(produto, -1);
        });

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve criar item com quantidade 1")
    void t6() {
        ItemPedido item = ItemPedido.criar(produto, 1);

        assertEquals(1, item.getQuantidade());
        assertEquals(new BigDecimal("15.00"), item.getValorTotal());
    }

    @Test
    @DisplayName("Deve recalcular valor total ao alterar quantidade")
    void t7() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        item.setQuantidade(5);
        item.calcularValorTotal();

        assertEquals(5, item.getQuantidade());
        assertEquals(new BigDecimal("75.00"), item.getValorTotal());
    }

    @Test
    @DisplayName("Deve criar item com quantidade grande")
    void t8() {
        ItemPedido item = ItemPedido.criar(produto, 100);

        assertEquals(100, item.getQuantidade());
        assertEquals(new BigDecimal("1500.00"), item.getValorTotal());
    }

    @Test
    @DisplayName("Deve verificar igualdade entre itens com mesmos dados")
    void t9() {
        ItemPedido item1 = ItemPedido.reconstituir(1L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));
        ItemPedido item2 = ItemPedido.reconstituir(1L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));

        assertEquals(item1, item2);
    }

    @Test
    @DisplayName("Deve verificar desigualdade entre itens com IDs diferentes")
    void t10() {
        ItemPedido item1 = ItemPedido.reconstituir(1L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));
        ItemPedido item2 = ItemPedido.reconstituir(2L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));

        assertNotEquals(item1, item2);
    }

    @Test
    @DisplayName("Deve verificar equals com objeto nulo")
    void t11() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        assertNotEquals(null, item);
    }

    @Test
    @DisplayName("Deve verificar equals com objeto de outra classe")
    void t12() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        assertNotEquals("String", item);
    }

    @Test
    @DisplayName("Deve verificar equals com o mesmo objeto")
    void t13() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        assertEquals(item, item);
    }

    @Test
    @DisplayName("Deve gerar hashCode consistente para itens iguais")
    void t14() {
        ItemPedido item1 = ItemPedido.reconstituir(1L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));
        ItemPedido item2 = ItemPedido.reconstituir(1L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));

        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    @DisplayName("Deve gerar toString com informações do item")
    void t15() {
        ItemPedido item = ItemPedido.reconstituir(1L, null, produto, 2, new BigDecimal("15.00"), new BigDecimal("30.00"));

        String toString = item.toString();

        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("2"));
        assertTrue(toString.contains("15.00"));
        assertTrue(toString.contains("30.00"));
    }

    @Test
    @DisplayName("Deve permitir definir ID do item")
    void t16() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        item.setId(10L);

        assertEquals(10L, item.getId());
    }

    @Test
    @DisplayName("Deve permitir alterar produto do item")
    void t17() {
        ItemPedido item = ItemPedido.criar(produto, 2);
        Produto novoProduto = Produto.reconstituir(2L, "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), Categoria.BEBIDA);

        item.setProduto(novoProduto);

        assertEquals(novoProduto, item.getProduto());
    }

    @Test
    @DisplayName("Deve permitir alterar valor unitário")
    void t18() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        item.setValorUnitario(new BigDecimal("20.00"));

        assertEquals(new BigDecimal("20.00"), item.getValorUnitario());
    }

    @Test
    @DisplayName("Deve permitir alterar valor total")
    void t19() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        item.setValorTotal(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("50.00"), item.getValorTotal());
    }

    @Test
    @DisplayName("Deve obter ID nulo quando não definido")
    void t20() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        assertNull(item.getId());
    }

    @Test
    @DisplayName("Deve obter pedido nulo quando não vinculado")
    void t21() {
        ItemPedido item = ItemPedido.criar(produto, 2);

        assertNull(item.getPedido());
    }
}
