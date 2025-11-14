package br.com.lanchonete.cozinha.adapters.messaging.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoAprovadoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio")
    void t1() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent();

        assertNotNull(event);
        assertNull(event.getPedidoId());
    }

    @Test
    @DisplayName("Deve criar evento com pedidoId")
    void t2() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(123L);

        assertEquals(123L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve definir pedidoId via setter")
    void t3() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent();
        event.setPedidoId(456L);

        assertEquals(456L, event.getPedidoId());
    }
}
