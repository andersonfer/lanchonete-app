package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteResponseDTOTest {

    @Test
    @DisplayName("Deve converter Cliente para ClienteResponseDTO corretamente")
    void t1() {

        Cliente cliente = Cliente.reconstituir(
                1L,
                "João Silva",
                "joao@email.com",
                "12345678901"
                );


        ClienteResponseDTO dto = ClienteResponseDTO.converterParaDTO(cliente);


        assertNotNull(dto, "O DTO não deveria ser nulo");
        assertEquals(1L, dto.id(), "O ID deveria ser 1L");
        assertEquals("João Silva", dto.nome(), "O nome não foi mapeado corretamente");
        assertEquals("joao@email.com", dto.email(), "O email não foi mapeado corretamente");
        assertEquals("12345678901", dto.cpf(), "O CPF não foi mapeado corretamente");
    }

    @Test
    @DisplayName("Deve retornar null quando Cliente for null")
    void t2() {
        ClienteResponseDTO dto = ClienteResponseDTO.converterParaDTO(null);
        assertNull(dto, "O DTO deveria ser nulo quando o cliente é nulo");
    }
}