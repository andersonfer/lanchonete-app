package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.IdentificarClienteUC;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IdentificarClienteController.class)
class IdentificarClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdentificarClienteUC identificarClienteUC;

    @Test
    @DisplayName("Deve identificar um cliente através de seu CPF")
    void t1() throws Exception {

        String cpf = "12345678901";

        ClienteResponseDTO resposta = ClienteResponseDTO.builder()
                .id(1L)
                .nome("Teste da Silva")
                .cpf(cpf)
                .email("teste@email.com")
                .build();

        when(identificarClienteUC.identificar(cpf)).thenReturn(Optional.of(resposta));

        mockMvc.perform(get("/clientes/cpf/{cpf}", cpf))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Teste da Silva"))
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }

    @Test
    @DisplayName("Deve retornar status 404 quando cliente não for encontrado pelo CPF")
    void t2() throws Exception {

        String cpfInexistente = "99999999999";

        when(identificarClienteUC.identificar(cpfInexistente)).thenReturn(Optional.empty());


        mockMvc.perform(get("/clientes/cpf/{cpf}", cpfInexistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar status 400 quando CPF for inválido")
    void t3() throws Exception {

        String cpfInvalido = "123";

        when(identificarClienteUC.identificar(anyString()))
                .thenThrow(new IllegalArgumentException("CPF deve conter 11 dígitos numéricos"));


        mockMvc.perform(get("/clientes/cpf/{cpf}", cpfInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CPF deve conter 11 dígitos numéricos"));
    }

}