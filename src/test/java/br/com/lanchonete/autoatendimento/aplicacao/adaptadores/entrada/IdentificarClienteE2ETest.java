package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Cliente;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IdentificarClienteE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    private Cliente clientePreCadastrado;

    private final String CPF_CLIENTE = "23456789012";

    @BeforeEach
    void configurar() {
        clientePreCadastrado = Cliente.builder()
                .nome("João dos Santos")
                .cpf(CPF_CLIENTE)
                .email("joao@email.com")
                .build();

        clientePreCadastrado = clienteRepositorio.salvar(clientePreCadastrado);
    }

    @Test
    @DisplayName("Deve identificar um cliente existente através do CPF")
    void t1() throws Exception {

        MvcResult resultado = mockMvc.perform(get("/clientes/cpf/{cpf}", CPF_CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientePreCadastrado.getId()))
                .andExpect(jsonPath("$.nome").value(clientePreCadastrado.getNome()))
                .andExpect(jsonPath("$.cpf").value(clientePreCadastrado.getCpf()))
                .andExpect(jsonPath("$.email").value(clientePreCadastrado.getEmail()))
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        ClienteResponseDTO respostaDTO = objectMapper.readValue(resposta, ClienteResponseDTO.class);

        assertEquals(clientePreCadastrado.getId(), respostaDTO.getId());
        assertEquals(clientePreCadastrado.getNome(), respostaDTO.getNome());
        assertEquals(clientePreCadastrado.getCpf(), respostaDTO.getCpf());
        assertEquals(clientePreCadastrado.getEmail(), respostaDTO.getEmail());
    }


    @Test
    @DisplayName("Deve retornar status 404 quando cliente não existe")
    void t2() throws Exception {

        mockMvc.perform(get("/clientes/cpf/{cpf}", "99988877766"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve identificar um cliente após ser cadastrado pela API")
    void t3() throws Exception {

        String novoCpf = "11122233344";
        CadastrarClienteDTO requisicao = CadastrarClienteDTO.builder()
                .nome("Carlos Santos")
                .email("carlos@email.com")
                .cpf(novoCpf)
                .build();


        //Cadastra um novo cliente via API
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isCreated());

        //Verifica se o cliente pode ser encontrado
        mockMvc.perform(get("/clientes/cpf/{cpf}", novoCpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(requisicao.getNome()))
                .andExpect(jsonPath("$.cpf").value(requisicao.getCpf()))
                .andExpect(jsonPath("$.email").value(requisicao.getEmail()));
    }

}
