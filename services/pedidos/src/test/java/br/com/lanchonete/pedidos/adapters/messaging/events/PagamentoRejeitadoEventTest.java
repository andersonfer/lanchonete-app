package br.com.lanchonete.pedidos.adapters.messaging.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoRejeitadoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio")
    void t1() {
        PagamentoRejeitadoEvent event = new PagamentoRejeitadoEvent();

        assertNotNull(event);
    }

    @Test
    @DisplayName("Deve criar evento com dados completos")
    void t2() {
        LocalDateTime agora = LocalDateTime.now();
        PagamentoRejeitadoEvent event = new PagamentoRejeitadoEvent(1L, agora);

        assertNotNull(event);
        assertEquals(1L, event.getPedidoId());
        assertEquals(agora, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir definir pedidoId")
    void t3() {
        PagamentoRejeitadoEvent event = new PagamentoRejeitadoEvent();

        event.setPedidoId(10L);

        assertEquals(10L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve permitir definir timestamp")
    void t4() {
        PagamentoRejeitadoEvent event = new PagamentoRejeitadoEvent();
        LocalDateTime agora = LocalDateTime.now();

        event.setTimestamp(agora);

        assertEquals(agora, event.getTimestamp());
    }
}
