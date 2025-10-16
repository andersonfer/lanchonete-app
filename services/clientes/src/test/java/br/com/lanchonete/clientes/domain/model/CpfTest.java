package br.com.lanchonete.clientes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    @Test
    @DisplayName("Deve criar CPF válido quando valor possui 11 dígitos")
    void t1() {
        String cpfValido = "12345678901";

        Cpf cpf = new Cpf(cpfValido);

        assertEquals(cpfValido, cpf.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF é nulo")
    void t2() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf(null);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF é vazio")
    void t3() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf("");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF é em branco")
    void t4() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf("   ");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF não possui 11 dígitos")
    void t5() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf("123456789");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF contém caracteres não numéricos")
    void t6() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cpf("123.456.789-01");
        });
    }

    @Test
    @DisplayName("Deve considerar dois CPFs iguais quando possuem mesmo valor")
    void t7() {
        Cpf cpf1 = new Cpf("12345678901");
        Cpf cpf2 = new Cpf("12345678901");

        assertEquals(cpf1, cpf2);
        assertEquals(cpf1.hashCode(), cpf2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois CPFs diferentes quando possuem valores diferentes")
    void t8() {
        Cpf cpf1 = new Cpf("12345678901");
        Cpf cpf2 = new Cpf("10987654321");

        assertNotEquals(cpf1, cpf2);
    }

    @Test
    @DisplayName("Deve retornar valor do CPF no toString")
    void t9() {
        String valorCpf = "12345678901";
        Cpf cpf = new Cpf(valorCpf);

        assertEquals(valorCpf, cpf.toString());
    }
}
