package br.com.lanchonete.pedidos.adapters.messaging.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoRetiradoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio com timestamp")
    void t1() {
        PedidoRetiradoEvent event = new PedidoRetiradoEvent();

        assertNotNull(event);
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve criar evento com pedidoId")
    void t2() {
        PedidoRetiradoEvent event = new PedidoRetiradoEvent(5L);

        assertNotNull(event);
        assertEquals(5L, event.getPedidoId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir definir pedidoId")
    void t3() {
        PedidoRetiradoEvent event = new PedidoRetiradoEvent();

        event.setPedidoId(10L);

        assertEquals(10L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve permitir definir timestamp")
    void t4() {
        PedidoRetiradoEvent event = new PedidoRetiradoEvent();
        LocalDateTime agora = LocalDateTime.now();

        event.setTimestamp(agora);

        assertEquals(agora, event.getTimestamp());
    }
}
