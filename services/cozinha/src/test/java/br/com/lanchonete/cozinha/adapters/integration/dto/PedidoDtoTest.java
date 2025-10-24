package br.com.lanchonete.cozinha.adapters.integration.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PedidoDtoTest {

    @Test
    @DisplayName("Deve criar PedidoDto vazio")
    void t1() {
        PedidoDto dto = new PedidoDto();

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getStatus());
    }

    @Test
    @DisplayName("Deve definir e obter ID")
    void t2() {
        PedidoDto dto = new PedidoDto();
        dto.setId(123L);

        assertEquals(123L, dto.getId());
    }

    @Test
    @DisplayName("Deve definir e obter status")
    void t3() {
        PedidoDto dto = new PedidoDto();
        dto.setStatus("PAGO");

        assertEquals("PAGO", dto.getStatus());
    }

    @Test
    @DisplayName("Deve definir todos os campos")
    void t4() {
        PedidoDto dto = new PedidoDto();
        dto.setId(456L);
        dto.setStatus("AGUARDANDO_PAGAMENTO");

        assertEquals(456L, dto.getId());
        assertEquals("AGUARDANDO_PAGAMENTO", dto.getStatus());
    }
}
