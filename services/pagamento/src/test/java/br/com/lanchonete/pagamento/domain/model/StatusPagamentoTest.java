package br.com.lanchonete.pagamento.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusPagamentoTest {

    @Test
    @DisplayName("Deve retornar true quando status é APROVADO")
    void t1() {
        StatusPagamento status = StatusPagamento.APROVADO;
        assertTrue(status.isAprovado());
    }

    @Test
    @DisplayName("Deve retornar false quando status APROVADO verifica isRejeitado")
    void t2() {
        StatusPagamento status = StatusPagamento.APROVADO;
        assertFalse(status.isRejeitado());
    }

    @Test
    @DisplayName("Deve retornar true quando status é REJEITADO")
    void t3() {
        StatusPagamento status = StatusPagamento.REJEITADO;
        assertTrue(status.isRejeitado());
    }

    @Test
    @DisplayName("Deve retornar false quando status REJEITADO verifica isAprovado")
    void t4() {
        StatusPagamento status = StatusPagamento.REJEITADO;
        assertFalse(status.isAprovado());
    }
}
