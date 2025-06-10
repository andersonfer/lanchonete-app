package br.com.lanchonete.autoatendimento.controllers;

import br.com.lanchonete.autoatendimento.controllers.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.IdentificarCliente;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CadastrarCliente cadastrarCliente;

    @MockitoBean
    private IdentificarCliente identificarCliente;

    private ClienteRequestDTO novoCliente;

    @BeforeEach
    void configurar() {
        novoCliente = new ClienteRequestDTO("Teste da Silva","12345678901","teste@email.com");
    }

    @Test
    @DisplayName("Deve cadastrar um cliente com sucesso")
    void t1() throws Exception {

        ClienteResponseDTO resposta = new ClienteResponseDTO(1L,
                "Teste da Silva","12345678901", "teste@email.com");


        when(cadastrarCliente.executar(any(ClienteRequestDTO.class))).thenReturn(resposta);

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Teste da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.email").value("teste@email.com"));


    }

    @Test
    @DisplayName("Deve retornar status 400 quando o UC lançar ValidacaoException")
    void t2() throws Exception {

        when(cadastrarCliente.executar(any(ClienteRequestDTO.class))).thenThrow(new ValidacaoException("Erro de validação ou duplicidade"));

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro de validação ou duplicidade"));
    }

    @Test
    @DisplayName("Deve identificar um cliente através de seu CPF")
    void t3() throws Exception {

        String cpf = "12345678901";

        ClienteResponseDTO resposta = new ClienteResponseDTO(1L,
                "Teste da Silva",cpf, "teste@email.com");

        when(identificarCliente.executar(cpf)).thenReturn(Optional.of(resposta));

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
    void t4() throws Exception {

        String cpfInexistente = "99999999999";

        when(identificarCliente.executar(cpfInexistente)).thenReturn(Optional.empty());


        mockMvc.perform(get("/clientes/cpf/{cpf}", cpfInexistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar status 400 quando CPF for inválido")
    void t5() throws Exception {

        String cpfInvalido = "123";

        when(identificarCliente.executar(anyString()))
                .thenThrow(new ValidacaoException("CPF deve conter 11 dígitos numéricos"));


        mockMvc.perform(get("/clientes/cpf/{cpf}", cpfInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CPF deve conter 11 dígitos numéricos"));
    }

}