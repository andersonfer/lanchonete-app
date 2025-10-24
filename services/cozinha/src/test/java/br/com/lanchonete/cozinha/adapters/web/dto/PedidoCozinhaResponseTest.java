package br.com.lanchonete.cozinha.adapters.web.dto;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PedidoCozinhaResponseTest {

    @Test
    @DisplayName("Deve criar response vazio")
    void t1() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();

        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getPedidoId());
        assertNull(response.getStatus());
        assertNull(response.getDataInicio());
        assertNull(response.getDataFim());
    }

    @Test
    @DisplayName("Deve definir e obter ID")
    void t2() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();
        response.setId(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Deve definir e obter pedidoId")
    void t3() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();
        response.setPedidoId(123L);

        assertEquals(123L, response.getPedidoId());
    }

    @Test
    @DisplayName("Deve definir e obter status")
    void t4() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();
        response.setStatus(StatusPedido.EM_PREPARO);

        assertEquals(StatusPedido.EM_PREPARO, response.getStatus());
    }

    @Test
    @DisplayName("Deve definir e obter dataInicio")
    void t5() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();
        LocalDateTime now = LocalDateTime.now();
        response.setDataInicio(now);

        assertEquals(now, response.getDataInicio());
    }

    @Test
    @DisplayName("Deve definir e obter dataFim")
    void t6() {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();
        LocalDateTime now = LocalDateTime.now();
        response.setDataFim(now);

        assertEquals(now, response.getDataFim());
    }
}
