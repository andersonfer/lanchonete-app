package br.com.lanchonete.pedidos.adapters.messaging.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoCriadoEventTest {

    @Test
    @DisplayName("Deve criar evento vazio com timestamp")
    void t1() {
        PedidoCriadoEvent event = new PedidoCriadoEvent();

        assertNotNull(event);
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve criar evento com dados completos")
    void t2() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(1L, "50.00", "12345678901");

        assertNotNull(event);
        assertEquals(1L, event.getPedidoId());
        assertEquals("50.00", event.getValor());
        assertEquals("12345678901", event.getCpfCliente());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir definir pedidoId")
    void t3() {
        PedidoCriadoEvent event = new PedidoCriadoEvent();

        event.setPedidoId(10L);

        assertEquals(10L, event.getPedidoId());
    }

    @Test
    @DisplayName("Deve permitir definir valor")
    void t4() {
        PedidoCriadoEvent event = new PedidoCriadoEvent();

        event.setValor("100.00");

        assertEquals("100.00", event.getValor());
    }

    @Test
    @DisplayName("Deve permitir definir cpfCliente")
    void t5() {
        PedidoCriadoEvent event = new PedidoCriadoEvent();

        event.setCpfCliente("98765432100");

        assertEquals("98765432100", event.getCpfCliente());
    }

    @Test
    @DisplayName("Deve permitir definir timestamp")
    void t6() {
        PedidoCriadoEvent event = new PedidoCriadoEvent();
        LocalDateTime agora = LocalDateTime.now();

        event.setTimestamp(agora);

        assertEquals(agora, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir cpfCliente nulo")
    void t7() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(1L, "50.00", null);

        assertNull(event.getCpfCliente());
    }
}
