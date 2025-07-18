package br.com.lanchonete.autoatendimento.adaptadores.rest.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private CadastrarCliente cadastrarCliente;

    @Mock
    private IdentificarCliente identificarCliente;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO novoCliente;
    private ClienteResponseDTO clienteResponse;
    private Cliente cliente;

    @BeforeEach
    void configurar() {
        novoCliente = new ClienteRequestDTO("Teste da Silva", "12345678901", "teste@email.com");
        clienteResponse = new ClienteResponseDTO(1L, "Teste da Silva", "12345678901", "teste@email.com");
        cliente = Cliente.reconstituir(1L, "Teste da Silva", "teste@email.com", "12345678901");
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso quando use case executa corretamente")
    void t1() {
        when(cadastrarCliente.executar(anyString(), anyString(), anyString())).thenReturn(cliente);

        ClienteResponseDTO resultado = clienteService.cadastrarCliente(novoCliente);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Teste da Silva", resultado.nome());
        assertEquals("12345678901", resultado.cpf());
        assertEquals("teste@email.com", resultado.email());
    }

    @Test
    @DisplayName("Deve propagar exceção quando use case lança ValidacaoException")
    void t2() {
        when(cadastrarCliente.executar(anyString(), anyString(), anyString()))
                .thenThrow(new ValidacaoException("Erro de validação"));

        assertThrows(ValidacaoException.class, () -> {
            clienteService.cadastrarCliente(novoCliente);
        });
    }

    @Test
    @DisplayName("Deve identificar cliente por CPF quando use case retorna cliente")
    void t3() {
        String cpf = "12345678901";
        when(identificarCliente.executar(cpf)).thenReturn(Optional.of(cliente));

        Optional<ClienteResponseDTO> resultado = clienteService.identificarPorCpf(cpf);

        assertTrue(resultado.isPresent());
        ClienteResponseDTO dto = resultado.get();
        assertEquals(cliente.getId(), dto.id());
        assertEquals(cliente.getNome(), dto.nome());
        assertEquals(cliente.getCpf().getValor(), dto.cpf());
        assertEquals(cliente.getEmail().getValor(), dto.email());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando use case não encontra cliente")
    void t4() {
        String cpfInexistente = "99999999999";
        when(identificarCliente.executar(cpfInexistente)).thenReturn(Optional.empty());

        Optional<ClienteResponseDTO> resultado = clienteService.identificarPorCpf(cpfInexistente);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve propagar exceção quando use case de identificação lança ValidacaoException")
    void t5() {
        String cpfInvalido = "123";
        when(identificarCliente.executar(anyString()))
                .thenThrow(new ValidacaoException("CPF deve conter 11 dígitos numéricos"));

        assertThrows(ValidacaoException.class, () -> {
            clienteService.identificarPorCpf(cpfInvalido);
        });
    }
}