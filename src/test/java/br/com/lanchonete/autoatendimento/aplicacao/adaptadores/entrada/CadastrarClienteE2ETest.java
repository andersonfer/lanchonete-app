package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;


import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Cliente;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastrarClienteE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Test
    @DisplayName("Deve cadastrar um cliente")

    void t1() throws Exception {

        CadastrarClienteDTO requisicao = CadastrarClienteDTO.builder()
                .nome("Carlos Santos")
                .email("carlos@email.com")
                .cpf("11122233344")
                .build();

        MvcResult resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isCreated())
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        ClienteResponseDTO respostaDTO = objectMapper.readValue(resposta, ClienteResponseDTO.class);

        //verifica se o ID foi gerado
        assertNotNull(respostaDTO.getId());
        assertEquals(requisicao.getNome(), respostaDTO.getNome());
        assertEquals(requisicao.getCpf(), respostaDTO.getCpf());
        assertEquals(requisicao.getEmail(), respostaDTO.getEmail());

        // Verifica se o cliente foi realmente persistido no banco de dados
        Optional<Cliente> clientePersistido = clienteRepositorio.buscarPorCpf(requisicao.getCpf());
        assertTrue(clientePersistido.isPresent());
        assertEquals(requisicao.getNome(), clientePersistido.get().getNome());
        assertEquals(requisicao.getEmail(), clientePersistido.get().getEmail());
    }

    @ParameterizedTest
    @MethodSource("fornecerCenariosClienteInvalido")
    @DisplayName("Deve retornar erro 400 ao tentar cadastrar cliente com dados inválidos")
    void t2(CadastrarClienteDTO requisicao, String mensagemErro) throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(mensagemErro));

        // Verifica que o cliente não foi persistido
        if (requisicao.getCpf() != null && !requisicao.getCpf().isEmpty()) {
            Optional<Cliente> clienteNaoPersistido = clienteRepositorio.buscarPorCpf(requisicao.getCpf());
            assertTrue(clienteNaoPersistido.isEmpty(), "O cliente não deveria ser persistido com dados inválidos");
        }
    }

    private static Stream<Arguments> fornecerCenariosClienteInvalido() {
        return Stream.of(
                // Nome inválido
                Arguments.of(criarDTO("", "98765432100", "paulo@email.com"), "Nome é obrigatório"),

                // CPF inválido - vazio
                Arguments.of(criarDTO("Paulo Silva", "", "paulo@email.com"), "CPF é obrigatório"),

                // CPF inválido - formato
                Arguments.of(criarDTO("Paulo Silva", "123", "paulo@email.com"), "CPF deve conter 11 dígitos numéricos"),
                Arguments.of(criarDTO("Paulo Silva", "1234567890A", "paulo@email.com"), "CPF deve conter 11 dígitos numéricos"),

                // Email inválido - vazio
                Arguments.of(criarDTO("Paulo Silva", "98765432100", ""), "Email é obrigatório"),

                // Email inválido - formato
                Arguments.of(criarDTO("Paulo Silva", "98765432100", "paulo"), "Email inválido"),
                Arguments.of(criarDTO("Paulo Silva", "98765432100", "paulo@"), "Email inválido")
        );
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar cadastrar cliente com CPF duplicado")
    void t3() throws Exception {

        CadastrarClienteDTO requisicao1 = CadastrarClienteDTO.builder()
                .nome("Carlos Santos")
                .email("carlos@email.com")
                .cpf("11122233344")
                .build();

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao1)))
                .andExpect(status().isCreated());

        //tentativa de cadastro duplicado
        CadastrarClienteDTO requisicao2 = CadastrarClienteDTO.builder()
                .nome("Outro Cliente")
                .email("outro@email.com")
                .cpf("11122233344")
                .build();

        MvcResult resultado = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requisicao2)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        assertEquals("CPF duplicado",resposta);
    }

    private static CadastrarClienteDTO criarDTO(String nome, String cpf, String email) {
        return CadastrarClienteDTO.builder()
                .nome(nome)
                .cpf(cpf)
                .email(email)
                .build();
    }
}
