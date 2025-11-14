package br.com.lanchonete.clientes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("Deve criar Email válido quando formato está correto")
    void t1() {
        String emailValido = "usuario@dominio.com";

        Email email = new Email(emailValido);

        assertEquals(emailValido, email.getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Email é nulo")
    void t2() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email(null);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Email é vazio")
    void t3() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Email é em branco")
    void t4() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("   ");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Email não possui @")
    void t5() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("usuariodominio.com");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Email não possui domínio")
    void t6() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("usuario@");
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando Email não possui usuário")
    void t7() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("@dominio.com");
        });
    }

    @Test
    @DisplayName("Deve considerar dois Emails iguais quando possuem mesmo valor")
    void t8() {
        Email email1 = new Email("teste@email.com");
        Email email2 = new Email("teste@email.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    @DisplayName("Deve considerar dois Emails diferentes quando possuem valores diferentes")
    void t9() {
        Email email1 = new Email("teste1@email.com");
        Email email2 = new Email("teste2@email.com");

        assertNotEquals(email1, email2);
    }

    @Test
    @DisplayName("Deve retornar valor do Email no toString")
    void t10() {
        String valorEmail = "teste@email.com";
        Email email = new Email(valorEmail);

        assertEquals(valorEmail, email.toString());
    }

    @Test
    @DisplayName("Deve aceitar Email com subdomínios")
    void t11() {
        String emailComSubdominio = "usuario@sub.dominio.com.br";

        Email email = new Email(emailComSubdominio);

        assertEquals(emailComSubdominio, email.getValor());
    }
}
