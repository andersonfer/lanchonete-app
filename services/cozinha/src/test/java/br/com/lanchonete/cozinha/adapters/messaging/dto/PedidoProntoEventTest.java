package br.com.lanchonete.cozinha.adapters.messaging.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidoProntoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio")
    void t1() {
        PedidoProntoEvent event = new PedidoProntoEvent();

        assertNotNull(event);
        assertNull(event.getPedidoId());
    }

    @Test
    @DisplayName("Deve criar evento com pedidoId")
    void t2() {
        PedidoProntoEvent event = new PedidoProntoEvent(123L);

        assertEquals(123L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve definir pedidoId via setter")
    void t3() {
        PedidoProntoEvent event = new PedidoProntoEvent();
        event.setPedidoId(456L);

        assertEquals(456L, event.getPedidoId());
    }
}
