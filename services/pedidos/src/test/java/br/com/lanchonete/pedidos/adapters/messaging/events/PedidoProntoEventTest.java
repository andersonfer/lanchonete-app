package br.com.lanchonete.pedidos.adapters.messaging.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoProntoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio")
    void t1() {
        PedidoProntoEvent event = new PedidoProntoEvent();

        assertNotNull(event);
    }

    @Test
    @DisplayName("Deve criar evento com dados completos")
    void t2() {
        LocalDateTime agora = LocalDateTime.now();
        PedidoProntoEvent event = new PedidoProntoEvent(1L, 100L, agora);

        assertNotNull(event);
        assertEquals(1L, event.getPedidoId());
        assertEquals(100L, event.getFilaId());
        assertEquals(agora, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir definir pedidoId")
    void t3() {
        PedidoProntoEvent event = new PedidoProntoEvent();

        event.setPedidoId(10L);

        assertEquals(10L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve permitir definir filaId")
    void t4() {
        PedidoProntoEvent event = new PedidoProntoEvent();

        event.setFilaId(200L);

        assertEquals(200L, event.getFilaId());
    }

    @Test
    @DisplayName("Deve permitir definir timestamp")
    void t5() {
        PedidoProntoEvent event = new PedidoProntoEvent();
        LocalDateTime agora = LocalDateTime.now();

        event.setTimestamp(agora);

        assertEquals(agora, event.getTimestamp());
    }
}
