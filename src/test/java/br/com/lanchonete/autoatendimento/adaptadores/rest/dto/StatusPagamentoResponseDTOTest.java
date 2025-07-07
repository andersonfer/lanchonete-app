package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusPagamentoResponseDTOTest {

    @Test
    @DisplayName("Deve criar DTO com status PENDENTE")
    void t1() {
        // Criar DTO com status PENDENTE
        StatusPagamentoResponseDTO dto = new StatusPagamentoResponseDTO(
                1L, 
                StatusPagamento.PENDENTE, 
                "Pagamento pendente de processamento"
        );

        // Verificações
        assertEquals(1L, dto.pedidoId());
        assertEquals(StatusPagamento.PENDENTE, dto.statusPagamento());
        assertEquals("Pagamento pendente de processamento", dto.mensagem());
    }

    @Test
    @DisplayName("Deve criar DTO com status APROVADO")
    void t2() {
        // Criar DTO com status APROVADO
        StatusPagamentoResponseDTO dto = new StatusPagamentoResponseDTO(
                2L, 
                StatusPagamento.APROVADO, 
                "Pagamento aprovado com sucesso"
        );

        // Verificações
        assertEquals(2L, dto.pedidoId());
        assertEquals(StatusPagamento.APROVADO, dto.statusPagamento());
        assertEquals("Pagamento aprovado com sucesso", dto.mensagem());
    }

    @Test
    @DisplayName("Deve criar DTO com status REJEITADO")
    void t3() {
        // Criar DTO com status REJEITADO
        StatusPagamentoResponseDTO dto = new StatusPagamentoResponseDTO(
                3L, 
                StatusPagamento.REJEITADO, 
                "Pagamento rejeitado"
        );

        // Verificações
        assertEquals(3L, dto.pedidoId());
        assertEquals(StatusPagamento.REJEITADO, dto.statusPagamento());
        assertEquals("Pagamento rejeitado", dto.mensagem());
    }

    @Test
    @DisplayName("Deve criar DTO usando método de fábrica para status PENDENTE")
    void t4() {
        // Criar DTO usando método de fábrica
        StatusPagamentoResponseDTO dto = StatusPagamentoResponseDTO.pendente(1L);

        // Verificações
        assertEquals(1L, dto.pedidoId());
        assertEquals(StatusPagamento.PENDENTE, dto.statusPagamento());
        assertEquals("Pagamento pendente de processamento", dto.mensagem());
    }

    @Test
    @DisplayName("Deve criar DTO usando método de fábrica para status APROVADO")
    void t5() {
        // Criar DTO usando método de fábrica
        StatusPagamentoResponseDTO dto = StatusPagamentoResponseDTO.aprovado(2L);

        // Verificações
        assertEquals(2L, dto.pedidoId());
        assertEquals(StatusPagamento.APROVADO, dto.statusPagamento());
        assertEquals("Pagamento aprovado com sucesso", dto.mensagem());
    }

    @Test
    @DisplayName("Deve criar DTO usando método de fábrica para status REJEITADO")
    void t6() {
        // Criar DTO usando método de fábrica
        StatusPagamentoResponseDTO dto = StatusPagamentoResponseDTO.rejeitado(3L);

        // Verificações
        assertEquals(3L, dto.pedidoId());
        assertEquals(StatusPagamento.REJEITADO, dto.statusPagamento());
        assertEquals("Pagamento rejeitado", dto.mensagem());
    }

    @Test
    @DisplayName("Deve criar DTO a partir de StatusPagamento usando método de fábrica")
    void t7() {
        // Testar todos os status usando método de fábrica genérico
        StatusPagamentoResponseDTO pendente = StatusPagamentoResponseDTO.de(1L, StatusPagamento.PENDENTE);
        StatusPagamentoResponseDTO aprovado = StatusPagamentoResponseDTO.de(2L, StatusPagamento.APROVADO);
        StatusPagamentoResponseDTO rejeitado = StatusPagamentoResponseDTO.de(3L, StatusPagamento.REJEITADO);

        // Verificações
        assertEquals(StatusPagamento.PENDENTE, pendente.statusPagamento());
        assertEquals("Pagamento pendente de processamento", pendente.mensagem());

        assertEquals(StatusPagamento.APROVADO, aprovado.statusPagamento());
        assertEquals("Pagamento aprovado com sucesso", aprovado.mensagem());

        assertEquals(StatusPagamento.REJEITADO, rejeitado.statusPagamento());
        assertEquals("Pagamento rejeitado", rejeitado.mensagem());
    }

    @Test
    @DisplayName("Deve verificar igualdade entre DTOs")
    void t8() {
        // Criar dois DTOs iguais
        StatusPagamentoResponseDTO dto1 = StatusPagamentoResponseDTO.aprovado(1L);
        StatusPagamentoResponseDTO dto2 = StatusPagamentoResponseDTO.aprovado(1L);

        // Criar DTO diferente
        StatusPagamentoResponseDTO dto3 = StatusPagamentoResponseDTO.rejeitado(1L);

        // Verificações
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter representação string apropriada")
    void t9() {
        // Criar DTO
        StatusPagamentoResponseDTO dto = StatusPagamentoResponseDTO.aprovado(123L);

        // Verificar toString
        String toString = dto.toString();
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("APROVADO"));
        assertTrue(toString.contains("Pagamento aprovado com sucesso"));
    }
}