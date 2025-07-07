package br.com.lanchonete.autoatendimento.dominio.modelo.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusPagamentoTest {

    @Test
    @DisplayName("Deve conter status PENDENTE")
    void t1() {
        // Verifica se o enum possui o valor PENDENTE
        StatusPagamento status = StatusPagamento.PENDENTE;
        assertNotNull(status);
        assertEquals("PENDENTE", status.name());
    }

    @Test
    @DisplayName("Deve conter status APROVADO")
    void t2() {
        // Verifica se o enum possui o valor APROVADO
        StatusPagamento status = StatusPagamento.APROVADO;
        assertNotNull(status);
        assertEquals("APROVADO", status.name());
    }

    @Test
    @DisplayName("Deve conter status REJEITADO")
    void t3() {
        // Verifica se o enum possui o valor REJEITADO
        StatusPagamento status = StatusPagamento.REJEITADO;
        assertNotNull(status);
        assertEquals("REJEITADO", status.name());
    }

    @Test
    @DisplayName("Deve conter exatamente 3 valores de status")
    void t4() {
        // Verifica se o enum possui exatamente 3 valores
        StatusPagamento[] valores = StatusPagamento.values();
        assertEquals(3, valores.length);
    }

    @Test
    @DisplayName("Deve permitir conversão de string para enum")
    void t5() {
        // Testa a conversão de string para enum
        StatusPagamento pendente = StatusPagamento.valueOf("PENDENTE");
        StatusPagamento aprovado = StatusPagamento.valueOf("APROVADO");
        StatusPagamento rejeitado = StatusPagamento.valueOf("REJEITADO");

        assertEquals(StatusPagamento.PENDENTE, pendente);
        assertEquals(StatusPagamento.APROVADO, aprovado);
        assertEquals(StatusPagamento.REJEITADO, rejeitado);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar converter string inválida para enum")
    void t6() {
        // Testa conversão de string inválida
        assertThrows(IllegalArgumentException.class, () -> {
            StatusPagamento.valueOf("INVALIDO");
        });
    }

    @Test
    @DisplayName("Deve verificar se status é pendente")
    void t7() {
        // Testa método utilitário isPendente (será implementado)
        StatusPagamento status = StatusPagamento.PENDENTE;
        assertTrue(status.isPendente());
        assertFalse(StatusPagamento.APROVADO.isPendente());
        assertFalse(StatusPagamento.REJEITADO.isPendente());
    }

    @Test
    @DisplayName("Deve verificar se status é aprovado")
    void t8() {
        // Testa método utilitário isAprovado (será implementado)
        StatusPagamento status = StatusPagamento.APROVADO;
        assertTrue(status.isAprovado());
        assertFalse(StatusPagamento.PENDENTE.isAprovado());
        assertFalse(StatusPagamento.REJEITADO.isAprovado());
    }

    @Test
    @DisplayName("Deve verificar se status é rejeitado")
    void t9() {
        // Testa método utilitário isRejeitado (será implementado)
        StatusPagamento status = StatusPagamento.REJEITADO;
        assertTrue(status.isRejeitado());
        assertFalse(StatusPagamento.PENDENTE.isRejeitado());
        assertFalse(StatusPagamento.APROVADO.isRejeitado());
    }

    @Test
    @DisplayName("Deve verificar se pagamento foi processado")
    void t10() {
        // Testa método utilitário foiProcessado (será implementado)
        // Processado = APROVADO ou REJEITADO
        assertTrue(StatusPagamento.APROVADO.foiProcessado());
        assertTrue(StatusPagamento.REJEITADO.foiProcessado());
        assertFalse(StatusPagamento.PENDENTE.foiProcessado());
    }
}