package br.com.lanchonete.cozinha.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoCozinhaTest {

    @Test
    @DisplayName("Deve criar PedidoCozinha com status AGUARDANDO e dataInicio preenchida")
    void t1() {
        Long pedidoId = 1L;
        LocalDateTime antes = LocalDateTime.now();

        PedidoCozinha fila = new PedidoCozinha(pedidoId);

        assertNotNull(fila);
        assertEquals(pedidoId, fila.getPedidoId());
        assertEquals(StatusPedido.AGUARDANDO, fila.getStatus());
        assertNotNull(fila.getDataInicio());
        assertTrue(fila.getDataInicio().isAfter(antes) || fila.getDataInicio().isEqual(antes));
        assertNull(fila.getDataFim());
        assertNull(fila.getId());
    }

    @Test
    @DisplayName("Deve iniciar preparo quando status for AGUARDANDO")
    void t2() {
        PedidoCozinha fila = new PedidoCozinha(1L);

        fila.iniciarPreparo();

        assertEquals(StatusPedido.EM_PREPARO, fila.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar preparo quando status não for AGUARDANDO")
    void t3() {
        PedidoCozinha fila = new PedidoCozinha(1L);
        fila.iniciarPreparo();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            fila::iniciarPreparo
        );

        assertEquals("Pedido não está aguardando preparo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve marcar como pronto quando status for EM_PREPARO")
    void t4() {
        PedidoCozinha fila = new PedidoCozinha(1L);
        fila.iniciarPreparo();
        LocalDateTime antes = LocalDateTime.now();

        fila.marcarComoPronto();

        assertEquals(StatusPedido.PRONTO, fila.getStatus());
        assertNotNull(fila.getDataFim());
        assertTrue(fila.getDataFim().isAfter(antes) || fila.getDataFim().isEqual(antes));
    }

    @Test
    @DisplayName("Deve lançar exceção ao marcar como pronto quando status não for EM_PREPARO")
    void t5() {
        PedidoCozinha fila = new PedidoCozinha(1L);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            fila::marcarComoPronto
        );

        assertEquals("Pedido não está em preparo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve permitir definir e obter ID")
    void t6() {
        PedidoCozinha fila = new PedidoCozinha(1L);
        Long id = 10L;

        fila.setId(id);

        assertEquals(id, fila.getId());
    }

    @Test
    @DisplayName("Deve permitir definir e obter status")
    void t7() {
        PedidoCozinha fila = new PedidoCozinha(1L);

        fila.setStatus(StatusPedido.PRONTO);

        assertEquals(StatusPedido.PRONTO, fila.getStatus());
    }

    @Test
    @DisplayName("Deve permitir definir e obter dataInicio")
    void t8() {
        PedidoCozinha fila = new PedidoCozinha(1L);
        LocalDateTime novaData = LocalDateTime.of(2025, 10, 23, 10, 0);

        fila.setDataInicio(novaData);

        assertEquals(novaData, fila.getDataInicio());
    }

    @Test
    @DisplayName("Deve permitir definir e obter dataFim")
    void t9() {
        PedidoCozinha fila = new PedidoCozinha(1L);
        LocalDateTime dataFim = LocalDateTime.of(2025, 10, 23, 12, 0);

        fila.setDataFim(dataFim);

        assertEquals(dataFim, fila.getDataFim());
    }

    @Test
    @DisplayName("Deve permitir definir pedidoId")
    void t10() {
        PedidoCozinha fila = new PedidoCozinha(1L);
        Long novoPedidoId = 999L;

        fila.setPedidoId(novoPedidoId);

        assertEquals(novoPedidoId, fila.getPedidoId());
    }
}
