package br.com.lanchonete.autoatendimento.entidades.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PrecoTest {

    @Test
    @DisplayName("Deve criar Preco válido quando valor é positivo")
    void t1() {
        BigDecimal valorValido = new BigDecimal("29.99");
        
        Preco preco = new Preco(valorValido);
        
        assertEquals(valorValido, preco.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Preco é nulo")
    void t2() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Preco(null);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Preco é zero")
    void t3() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Preco(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Preco é negativo")
    void t4() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Preco(new BigDecimal("-1.00"));
        });
    }

    @Test
    @DisplayName("Deve aceitar Preco com valor muito pequeno positivo")
    void t5() {
        BigDecimal valorPequeno = new BigDecimal("0.01");
        
        Preco preco = new Preco(valorPequeno);
        
        assertEquals(valorPequeno, preco.getValor());
    }

    @Test
    @DisplayName("Deve aceitar Preco com valor muito grande")
    void t6() {
        BigDecimal valorGrande = new BigDecimal("999999.99");
        
        Preco preco = new Preco(valorGrande);
        
        assertEquals(valorGrande, preco.getValor());
    }

    @Test
    @DisplayName("Deve considerar dois Precos iguais quando possuem mesmo valor")
    void t7() {
        Preco preco1 = new Preco(new BigDecimal("19.99"));
        Preco preco2 = new Preco(new BigDecimal("19.99"));
        
        assertEquals(preco1, preco2);
        assertEquals(preco1.hashCode(), preco2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois Precos diferentes quando possuem valores diferentes")
    void t8() {
        Preco preco1 = new Preco(new BigDecimal("19.99"));
        Preco preco2 = new Preco(new BigDecimal("29.99"));
        
        assertNotEquals(preco1, preco2);
    }

    @Test
    @DisplayName("Deve retornar valor do Preco no toString")
    void t9() {
        BigDecimal valorPreco = new BigDecimal("15.50");
        Preco preco = new Preco(valorPreco);
        
        assertEquals(valorPreco.toString(), preco.toString());
    }

    @Test
    @DisplayName("Deve aceitar Preco criado com string válida")
    void t10() {
        Preco preco = new Preco(new BigDecimal("12.34"));
        
        assertEquals(new BigDecimal("12.34"), preco.getValor());
    }
}