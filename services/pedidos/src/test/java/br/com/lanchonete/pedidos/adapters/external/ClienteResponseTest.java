package br.com.lanchonete.pedidos.adapters.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteResponseTest {

    @Test
    @DisplayName("Deve criar ClienteResponse vazio")
    void t1() {
        ClienteResponse response = new ClienteResponse();

        assertNotNull(response);
    }

    @Test
    @DisplayName("Deve criar ClienteResponse com dados completos")
    void t2() {
        ClienteResponse response = new ClienteResponse(1L, "12345678901", "João Silva", "joao@email.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("12345678901", response.getCpf());
        assertEquals("João Silva", response.getNome());
        assertEquals("joao@email.com", response.getEmail());
    }

    @Test
    @DisplayName("Deve permitir definir id")
    void t3() {
        ClienteResponse response = new ClienteResponse();

        response.setId(10L);

        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("Deve permitir definir cpf")
    void t4() {
        ClienteResponse response = new ClienteResponse();

        response.setCpf("98765432100");

        assertEquals("98765432100", response.getCpf());
    }

    @Test
    @DisplayName("Deve permitir definir nome")
    void t5() {
        ClienteResponse response = new ClienteResponse();

        response.setNome("Maria Souza");

        assertEquals("Maria Souza", response.getNome());
    }

    @Test
    @DisplayName("Deve permitir definir email")
    void t6() {
        ClienteResponse response = new ClienteResponse();

        response.setEmail("maria@email.com");

        assertEquals("maria@email.com", response.getEmail());
    }
}
