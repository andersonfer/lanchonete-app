package br.com.lanchonete.pagamento.domain.model;

import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValorTest {

    @Test
    @DisplayName("Deve criar Valor com sucesso quando BigDecimal é válido")
    void t1() {
        BigDecimal valorValido = new BigDecimal("100.00");
        Valor valor = new Valor(valorValido);
        assertEquals(valorValido, valor.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é nulo")
    void t2() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Valor(null);
        });
        assertEquals("Valor não pode ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é zero")
    void t3() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Valor(BigDecimal.ZERO);
        });
        assertEquals("Valor deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é negativo")
    void t4() {
        ValidacaoException exception = assertThrows(ValidacaoException.class, () -> {
            new Valor(new BigDecimal("-10.00"));
        });
        assertEquals("Valor deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar true quando dois Valores são iguais")
    void t5() {
        Valor valor1 = new Valor(new BigDecimal("50.00"));
        Valor valor2 = new Valor(new BigDecimal("50.00"));
        assertEquals(valor1, valor2);
    }

    @Test
    @DisplayName("Deve retornar false quando dois Valores são diferentes")
    void t6() {
        Valor valor1 = new Valor(new BigDecimal("50.00"));
        Valor valor2 = new Valor(new BigDecimal("100.00"));
        assertNotEquals(valor1, valor2);
    }

    @Test
    @DisplayName("Deve retornar string correta no toString")
    void t7() {
        Valor valor = new Valor(new BigDecimal("75.50"));
        assertEquals("75.50", valor.toString());
    }
}
