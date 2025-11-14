package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumeroPedidoTest {

    @Test
    @DisplayName("Deve gerar número de pedido por ID válido")
    void t1() {
        Long id = 1L;

        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(id);

        assertEquals("PED-000001", numeroPedido.getValor());
    }

    @Test
    @DisplayName("Deve gerar número de pedido formatado com 6 dígitos")
    void t2() {
        Long id = 123L;

        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(id);

        assertEquals("PED-000123", numeroPedido.getValor());
    }

    @Test
    @DisplayName("Deve gerar número de pedido para IDs grandes")
    void t3() {
        Long id = 999999L;

        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(id);

        assertEquals("PED-999999", numeroPedido.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID é nulo")
    void t4() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            NumeroPedido.gerarPorId(null);
        });

        assertEquals("ID deve ser um valor positivo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID é zero")
    void t5() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            NumeroPedido.gerarPorId(0L);
        });

        assertEquals("ID deve ser um valor positivo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID é negativo")
    void t6() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            NumeroPedido.gerarPorId(-1L);
        });

        assertEquals("ID deve ser um valor positivo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve considerar dois números iguais quando possuem mesmo valor")
    void t7() {
        NumeroPedido numero1 = NumeroPedido.gerarPorId(10L);
        NumeroPedido numero2 = NumeroPedido.gerarPorId(10L);

        assertEquals(numero1, numero2);
        assertEquals(numero1.hashCode(), numero2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois números diferentes quando possuem valores diferentes")
    void t8() {
        NumeroPedido numero1 = NumeroPedido.gerarPorId(10L);
        NumeroPedido numero2 = NumeroPedido.gerarPorId(20L);

        assertNotEquals(numero1, numero2);
    }

    @Test
    @DisplayName("Deve retornar valor do número no toString")
    void t9() {
        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(5L);

        assertEquals("PED-000005", numeroPedido.toString());
    }
}
