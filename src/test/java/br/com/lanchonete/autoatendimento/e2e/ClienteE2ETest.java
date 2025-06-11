package br.com.lanchonete.autoatendimento.e2e;


import br.com.lanchonete.autoatendimento.controllers.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteGateway clienteGateway;

    @Test
    @DisplayName("Deve cadastrar um cliente")

    void t1() throws Exception {

        ClienteRequestDTO novoCliente = new ClienteRequestDTO("Carlos Santos","11122233344","carlos@email.com");

        MvcResult resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isCreated())
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        ClienteResponseDTO respostaDTO = objectMapper.readValue(resposta, ClienteResponseDTO.class);

        //verifica se o ID foi gerado
        assertNotNull(respostaDTO.id());
        assertEquals(novoCliente.nome(), respostaDTO.nome());
        assertEquals(novoCliente.cpf(), respostaDTO.cpf());
        assertEquals(novoCliente.email(), respostaDTO.email());

        // Verifica se o cliente foi realmente persistido no banco de dados
        Optional<Cliente> clientePersistido = clienteGateway.buscarPorCpf(novoCliente.cpf());
        assertTrue(clientePersistido.isPresent());
        assertEquals(novoCliente.nome(), clientePersistido.get().getNome());
        assertEquals(novoCliente.email(), clientePersistido.get().getEmail().getValor());
    }

    @ParameterizedTest
    @MethodSource("fornecerCenariosClienteInvalido")
    @DisplayName("Deve retornar erro 400 ao tentar cadastrar cliente com dados inválidos")
    void t2(ClienteRequestDTO requisicao, String mensagemErro) throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(mensagemErro));

        // Verifica que o cliente não foi persistido
        if (requisicao.cpf() != null && !requisicao.cpf().isEmpty()) {
            Optional<Cliente> clienteNaoPersistido = clienteGateway.buscarPorCpf(requisicao.cpf());
            assertTrue(clienteNaoPersistido.isEmpty(), "O cliente não deveria ser persistido com dados inválidos");
        }
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar cadastrar cliente com CPF duplicado")
    void t3() throws Exception {

        ClienteRequestDTO novoCliente = new ClienteRequestDTO("Carlos Santos","11122233344","carlos@email.com");

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isCreated());

        //tentativa de cadastro duplicado
        ClienteRequestDTO novoClienteComCpfDuplicado = new ClienteRequestDTO("Outro Cliente","11122233344","outro@email.com");

        MvcResult resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoClienteComCpfDuplicado)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        assertEquals("CPF duplicado",resposta);
    }

    @Test
    @DisplayName("Deve identificar um cliente existente através do CPF")
    void t4() throws Exception {

        Cliente clientePreCadastrado = Cliente.criar(
                "João dos Santos",
                "joao@email.com",
                "23456789012");

        clientePreCadastrado = clienteGateway.salvar(clientePreCadastrado);

        MvcResult resultado = mockMvc.perform(get("/clientes/cpf/{cpf}", "23456789012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientePreCadastrado.getId()))
                .andExpect(jsonPath("$.nome").value(clientePreCadastrado.getNome()))
                .andExpect(jsonPath("$.cpf").value(clientePreCadastrado.getCpf().getValor()))
                .andExpect(jsonPath("$.email").value(clientePreCadastrado.getEmail().getValor()))
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        ClienteResponseDTO respostaDTO = objectMapper.readValue(resposta, ClienteResponseDTO.class);

        assertEquals(clientePreCadastrado.getId(), respostaDTO.id());
        assertEquals(clientePreCadastrado.getNome(), respostaDTO.nome());
        assertEquals(clientePreCadastrado.getCpf().getValor(), respostaDTO.cpf());
        assertEquals(clientePreCadastrado.getEmail().getValor(), respostaDTO.email());
    }


    @Test
    @DisplayName("Deve retornar status 404 quando cliente não existe")
    void t5() throws Exception {

        mockMvc.perform(get("/clientes/cpf/{cpf}", "99988877766"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve identificar um cliente após ser cadastrado pela API")
    void t6() throws Exception {

        String novoCpf = "11122233344";
        ClienteRequestDTO novoCliente = new ClienteRequestDTO("Carlos Santos",novoCpf,"carlos@email.com");

        //Cadastra um novo cliente via API
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isCreated());

        //Verifica se o cliente pode ser encontrado
        mockMvc.perform(get("/clientes/cpf/{cpf}", novoCpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(novoCliente.nome()))
                .andExpect(jsonPath("$.cpf").value(novoCliente.cpf()))
                .andExpect(jsonPath("$.email").value(novoCliente.email()));
    }

    private static Stream<Arguments> fornecerCenariosClienteInvalido() {
        return Stream.of(
                // Nome inválido
                Arguments.of(new ClienteRequestDTO("", "98765432100", "paulo@email.com"), "Nome é obrigatório"),

                // CPF inválido - vazio
                Arguments.of(new ClienteRequestDTO("Paulo Silva", "", "paulo@email.com"), "CPF é obrigatório"),

                // CPF inválido - formato
                Arguments.of(new ClienteRequestDTO("Paulo Silva", "123", "paulo@email.com"), "CPF deve conter 11 dígitos numéricos"),
                Arguments.of(new ClienteRequestDTO("Paulo Silva", "1234567890A", "paulo@email.com"), "CPF deve conter 11 dígitos numéricos"),

                // Email inválido - vazio
                Arguments.of(new ClienteRequestDTO("Paulo Silva", "98765432100", ""), "Email é obrigatório"),

                // Email inválido - formato
                Arguments.of(new ClienteRequestDTO("Paulo Silva", "98765432100", "paulo"), "Email inválido"),
                Arguments.of(new ClienteRequestDTO("Paulo Silva", "98765432100", "paulo@"), "Email inválido")
        );
    }

}
