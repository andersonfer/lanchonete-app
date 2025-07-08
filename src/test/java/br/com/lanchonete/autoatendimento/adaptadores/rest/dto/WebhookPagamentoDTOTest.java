package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookPagamentoDTOTest {

    @Test
    @DisplayName("Deve criar DTO com pedidoId e statusPagamento APROVADO")
    void t1() {
        // Arrange & Act
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(1L, "APROVADO");
        
        // Assert
        assertEquals(1L, dto.pedidoId());
        assertEquals("APROVADO", dto.statusPagamento());
    }

    @Test
    @DisplayName("Deve criar DTO com pedidoId e statusPagamento REJEITADO")
    void t2() {
        // Arrange & Act
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(2L, "REJEITADO");
        
        // Assert
        assertEquals(2L, dto.pedidoId());
        assertEquals("REJEITADO", dto.statusPagamento());
    }

    @Test
    @DisplayName("Deve permitir pedidoId null")
    void t3() {
        // Arrange & Act
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(null, "APROVADO");
        
        // Assert
        assertNull(dto.pedidoId());
        assertEquals("APROVADO", dto.statusPagamento());
    }

    @Test
    @DisplayName("Deve permitir statusPagamento null")
    void t4() {
        // Arrange & Act
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(1L, null);
        
        // Assert
        assertEquals(1L, dto.pedidoId());
        assertNull(dto.statusPagamento());
    }

    @Test
    @DisplayName("Deve permitir ambos campos null")
    void t5() {
        // Arrange & Act
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(null, null);
        
        // Assert
        assertNull(dto.pedidoId());
        assertNull(dto.statusPagamento());
    }

    @Test
    @DisplayName("Deve aceitar statusPagamento com diferentes valores")
    void t6() {
        // Arrange & Act
        WebhookPagamentoDTO dto1 = new WebhookPagamentoDTO(1L, "APROVADO");
        WebhookPagamentoDTO dto2 = new WebhookPagamentoDTO(2L, "REJEITADO");
        WebhookPagamentoDTO dto3 = new WebhookPagamentoDTO(3L, "PENDENTE");
        
        // Assert
        assertEquals("APROVADO", dto1.statusPagamento());
        assertEquals("REJEITADO", dto2.statusPagamento());
        assertEquals("PENDENTE", dto3.statusPagamento());
    }

    @Test
    @DisplayName("Deve aceitar statusPagamento com valores inválidos")
    void t7() {
        // Arrange & Act
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(1L, "INVALIDO");
        
        // Assert
        assertEquals(1L, dto.pedidoId());
        assertEquals("INVALIDO", dto.statusPagamento());
    }

    @Test
    @DisplayName("Deve ter equals e hashCode funcionando corretamente")
    void t8() {
        // Arrange
        WebhookPagamentoDTO dto1 = new WebhookPagamentoDTO(1L, "APROVADO");
        WebhookPagamentoDTO dto2 = new WebhookPagamentoDTO(1L, "APROVADO");
        WebhookPagamentoDTO dto3 = new WebhookPagamentoDTO(2L, "APROVADO");
        
        // Assert
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString funcionando corretamente")
    void t9() {
        // Arrange
        WebhookPagamentoDTO dto = new WebhookPagamentoDTO(1L, "APROVADO");
        
        // Act
        String resultado = dto.toString();
        
        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("1"));
        assertTrue(resultado.contains("APROVADO"));
    }
}