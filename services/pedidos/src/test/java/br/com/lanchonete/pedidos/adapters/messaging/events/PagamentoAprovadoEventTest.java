package br.com.lanchonete.pedidos.adapters.messaging.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoAprovadoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio")
    void t1() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent();

        assertNotNull(event);
    }

    @Test
    @DisplayName("Deve criar evento com dados completos")
    void t2() {
        LocalDateTime agora = LocalDateTime.now();
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(1L, agora);

        assertNotNull(event);
        assertEquals(1L, event.getPedidoId());
        assertEquals(agora, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir definir pedidoId")
    void t3() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent();

        event.setPedidoId(10L);

        assertEquals(10L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve permitir definir timestamp")
    void t4() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent();
        LocalDateTime agora = LocalDateTime.now();

        event.setTimestamp(agora);

        assertEquals(agora, event.getTimestamp());
    }
}
