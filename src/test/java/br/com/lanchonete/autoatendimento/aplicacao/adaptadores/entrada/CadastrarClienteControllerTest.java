package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.CadastrarClienteUC;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CadastrarClienteController.class)
class CadastrarClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CadastrarClienteUC cadastrarClienteUC;

    private CadastrarClienteDTO requisicao;

    @BeforeEach
    void configurar() {
        requisicao = CadastrarClienteDTO.builder()
                .nome("Teste da Silva")
                .cpf("12345678901")
                .email("teste@email.com")
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar um cliente com sucesso")
    void t1() throws Exception {

        ClienteResponseDTO resposta = ClienteResponseDTO.builder()
                .id(1L)
                .nome("Teste da Silva")
                .cpf("12345678901")
                .email("teste@email.com")
                .build();

        when(cadastrarClienteUC.cadastrar(any(CadastrarClienteDTO.class))).thenReturn(resposta);

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Teste da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.email").value("teste@email.com"));


    }

    @Test
    @DisplayName("Deve retornar status 400 quando o UC lançar IllegalArgumentException")
    void t2() throws Exception {

        when(cadastrarClienteUC.cadastrar(any(CadastrarClienteDTO.class))).thenThrow(new IllegalArgumentException("Erro de validação ou duplicidade"));

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro de validação ou duplicidade"));
    }

}