package br.com.lanchonete.autoatendimento.dominio.modelo.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumeroPedidoTest {

    @Test
    @DisplayName("Deve gerar número por ID corretamente")
    void t1() {
        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(123L);
        
        assertEquals("PED000123", numeroPedido.getValor());
    }

    @Test
    @DisplayName("Deve gerar número por ID com formatação de 6 dígitos")
    void t2() {
        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(1L);
        
        assertEquals("PED000001", numeroPedido.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar por ID nulo")
    void t3() {
        assertThrows(IllegalArgumentException.class, () -> {
            NumeroPedido.gerarPorId(null);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar por ID zero")
    void t4() {
        assertThrows(IllegalArgumentException.class, () -> {
            NumeroPedido.gerarPorId(0L);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar por ID negativo")
    void t5() {
        assertThrows(IllegalArgumentException.class, () -> {
            NumeroPedido.gerarPorId(-1L);
        });
    }

    @Test
    @DisplayName("Deve gerar número com formato correto para ID grande")
    void t6() {
        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(999999L);
        
        assertEquals("PED999999", numeroPedido.getValor());
    }

    @Test
    @DisplayName("Deve gerar números diferentes para IDs diferentes")
    void t7() {
        NumeroPedido numero1 = NumeroPedido.gerarPorId(1L);
        NumeroPedido numero2 = NumeroPedido.gerarPorId(2L);
        
        assertNotEquals(numero1.getValor(), numero2.getValor());
    }

    @Test
    @DisplayName("Deve considerar dois NumeroPedidos iguais quando gerados com mesmo ID")
    void t8() {
        NumeroPedido numero1 = NumeroPedido.gerarPorId(123L);
        NumeroPedido numero2 = NumeroPedido.gerarPorId(123L);
        
        assertEquals(numero1, numero2);
        assertEquals(numero1.hashCode(), numero2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois NumeroPedidos diferentes quando gerados com IDs diferentes")
    void t9() {
        NumeroPedido numero1 = NumeroPedido.gerarPorId(123L);
        NumeroPedido numero2 = NumeroPedido.gerarPorId(456L);
        
        assertNotEquals(numero1, numero2);
    }

    @Test
    @DisplayName("Deve retornar valor do NumeroPedido no toString")
    void t10() {
        NumeroPedido numeroPedido = NumeroPedido.gerarPorId(123L);
        
        assertEquals("PED000123", numeroPedido.toString());
    }

}