package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusPedidoTest {

    @Test
    @DisplayName("Deve ter valor CRIADO")
    void t1() {
        assertEquals("CRIADO", StatusPedido.CRIADO.name());
    }

    @Test
    @DisplayName("Deve ter valor REALIZADO")
    void t2() {
        assertEquals("REALIZADO", StatusPedido.REALIZADO.name());
    }

    @Test
    @DisplayName("Deve ter valor CANCELADO")
    void t3() {
        assertEquals("CANCELADO", StatusPedido.CANCELADO.name());
    }

    @Test
    @DisplayName("Deve ter valor EM_PREPARACAO")
    void t4() {
        assertEquals("EM_PREPARACAO", StatusPedido.EM_PREPARACAO.name());
    }

    @Test
    @DisplayName("Deve ter valor PRONTO")
    void t5() {
        assertEquals("PRONTO", StatusPedido.PRONTO.name());
    }

    @Test
    @DisplayName("Deve ter valor FINALIZADO")
    void t6() {
        assertEquals("FINALIZADO", StatusPedido.FINALIZADO.name());
    }

    @Test
    @DisplayName("Deve converter string para enum")
    void t7() {
        StatusPedido status = StatusPedido.valueOf("CRIADO");
        assertEquals(StatusPedido.CRIADO, status);
    }

    @Test
    @DisplayName("Deve retornar todos os valores do enum")
    void t8() {
        StatusPedido[] valores = StatusPedido.values();
        assertEquals(6, valores.length);
    }
}
