package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PrecoTest {

    @Test
    @DisplayName("Deve criar preço válido quando valor é positivo")
    void t1() {
        BigDecimal valorValido = new BigDecimal("10.50");

        Preco preco = new Preco(valorValido);

        assertEquals(valorValido, preco.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é nulo")
    void t2() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Preco(null);
        });

        assertEquals("Preço é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é zero")
    void t3() {
        BigDecimal valorZero = BigDecimal.ZERO;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Preco(valorZero);
        });

        assertEquals("Preço deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é negativo")
    void t4() {
        BigDecimal valorNegativo = new BigDecimal("-5.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Preco(valorNegativo);
        });

        assertEquals("Preço deve ser maior que zero", exception.getMessage());
    }

    @Test
    @DisplayName("Deve considerar dois preços iguais quando possuem mesmo valor")
    void t5() {
        Preco preco1 = new Preco(new BigDecimal("15.00"));
        Preco preco2 = new Preco(new BigDecimal("15.00"));

        assertEquals(preco1, preco2);
        assertEquals(preco1.hashCode(), preco2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois preços diferentes quando possuem valores diferentes")
    void t6() {
        Preco preco1 = new Preco(new BigDecimal("15.00"));
        Preco preco2 = new Preco(new BigDecimal("20.00"));

        assertNotEquals(preco1, preco2);
    }

    @Test
    @DisplayName("Deve retornar valor do preço no toString")
    void t7() {
        BigDecimal valor = new BigDecimal("25.50");
        Preco preco = new Preco(valor);

        assertEquals("25.50", preco.toString());
    }

    @Test
    @DisplayName("Deve aceitar preço com várias casas decimais")
    void t8() {
        BigDecimal valor = new BigDecimal("99.999");
        Preco preco = new Preco(valor);

        assertEquals(valor, preco.getValor());
    }
}
