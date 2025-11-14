package br.com.lanchonete.cozinha.adapters.web.dto;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoCozinhaResponseTest {

    @Test
    @DisplayName("Deve criar response com todos os campos")
    void t1() {
        LocalDateTime dataInicio = LocalDateTime.now();
        LocalDateTime dataFim = LocalDateTime.now().plusHours(1);

        PedidoCozinhaResponse response = new PedidoCozinhaResponse(
            1L,
            123L,
            StatusPedido.EM_PREPARO,
            dataInicio,
            dataFim
        );

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(123L, response.pedidoId());
        assertEquals(StatusPedido.EM_PREPARO, response.status());
        assertEquals(dataInicio, response.dataInicio());
        assertEquals(dataFim, response.dataFim());
    }

    @Test
    @DisplayName("Deve criar response com valores nulos")
    void t2() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse(
            null,
            null,
            null,
            null,
            null
        );

        assertNotNull(response);
        assertNull(response.id());
        assertNull(response.pedidoId());
        assertNull(response.status());
        assertNull(response.dataInicio());
        assertNull(response.dataFim());
    }

    @Test
    @DisplayName("Deve criar response a partir do domínio")
    void t3() {
        PedidoCozinha pedido = new PedidoCozinha(123L);
        pedido.setId(1L);
        pedido.iniciarPreparo();

        PedidoCozinhaResponse response = PedidoCozinhaResponse.fromDomain(pedido);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(123L, response.pedidoId());
        assertEquals(StatusPedido.EM_PREPARO, response.status());
        assertNotNull(response.dataInicio());
        assertNull(response.dataFim());
    }

    @Test
    @DisplayName("Deve criar response a partir do domínio com pedido pronto")
    void t4() {
        PedidoCozinha pedido = new PedidoCozinha(456L);
        pedido.setId(2L);
        pedido.iniciarPreparo();
        pedido.marcarComoPronto();

        PedidoCozinhaResponse response = PedidoCozinhaResponse.fromDomain(pedido);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals(456L, response.pedidoId());
        assertEquals(StatusPedido.PRONTO, response.status());
        assertNotNull(response.dataInicio());
        assertNotNull(response.dataFim());
    }

    @Test
    @DisplayName("Deve permitir acesso aos campos do record")
    void t5() {
        LocalDateTime now = LocalDateTime.now();
        PedidoCozinhaResponse response = new PedidoCozinhaResponse(
            10L,
            999L,
            StatusPedido.AGUARDANDO,
            now,
            null
        );

        assertEquals(10L, response.id());
        assertEquals(999L, response.pedidoId());
        assertEquals(StatusPedido.AGUARDANDO, response.status());
        assertEquals(now, response.dataInicio());
        assertNull(response.dataFim());
    }
}
