package br.com.lanchonete.auth.casosdeuso;

import br.com.lanchonete.auth.exception.ClienteNaoEncontradoException;
import br.com.lanchonete.auth.exception.CpfInvalidoException;
import br.com.lanchonete.auth.gateway.ClienteGateway;
import br.com.lanchonete.auth.model.Cliente;
import br.com.lanchonete.auth.util.CpfValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AutenticarClienteTest {

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private CpfValidator cpfValidator;

    private AutenticarCliente autenticarCliente;

    private Cliente clienteTeste;

    @BeforeEach
    void configurar() {
        MockitoAnnotations.openMocks(this);
        autenticarCliente = new AutenticarCliente(clienteGateway, cpfValidator);
        clienteTeste = new Cliente(1L, "João Silva", "joao@email.com", "12345678901");
    }

    @Test
    @DisplayName("Deve autenticar cliente com CPF válido com sucesso")
    void t1() throws Exception {
        String cpf = "12345678901";
        
        when(cpfValidator.isValido(cpf)).thenReturn(true);
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.of(clienteTeste));

        Cliente resultado = autenticarCliente.executar(cpf);

        assertNotNull(resultado, "Cliente não deveria ser nulo");
        assertEquals(1L, resultado.getId(), "ID do cliente deve estar correto");
        assertEquals("João Silva", resultado.getNome(), "Nome do cliente deve estar correto");
        assertEquals("joao@email.com", resultado.getEmail(), "Email do cliente deve estar correto");
        assertEquals(cpf, resultado.getCpf(), "CPF do cliente deve estar correto");

        verify(cpfValidator).isValido(cpf);
        verify(clienteGateway).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF inválido")
    void t2() throws Exception {
        String cpfInvalido = "12345";
        
        when(cpfValidator.isValido(cpfInvalido)).thenReturn(false);

        CpfInvalidoException exception = assertThrows(
                CpfInvalidoException.class,
                () -> autenticarCliente.executar(cpfInvalido),
                "Deve lançar exceção para CPF inválido"
        );

        assertEquals("CPF inválido: " + cpfInvalido, exception.getMessage(),
                "Mensagem de erro deve estar correta");

        verify(cpfValidator).isValido(cpfInvalido);
        verify(clienteGateway, never()).buscarPorCpf(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para cliente não encontrado")
    void t3() throws Exception {
        String cpf = "99999999999";
        
        when(cpfValidator.isValido(cpf)).thenReturn(true);
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.empty());

        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> autenticarCliente.executar(cpf),
                "Deve lançar exceção para cliente não encontrado"
        );

        assertEquals("Cliente não encontrado para CPF: " + cpf, exception.getMessage(),
                "Mensagem de erro deve estar correta");

        verify(cpfValidator).isValido(cpf);
        verify(clienteGateway).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF nulo")
    void t4() throws Exception {
        when(cpfValidator.isValido(null)).thenReturn(false);

        CpfInvalidoException exception = assertThrows(
                CpfInvalidoException.class,
                () -> autenticarCliente.executar(null),
                "Deve lançar exceção para CPF nulo"
        );

        assertEquals("CPF inválido: null", exception.getMessage(),
                "Mensagem de erro deve estar correta");

        verify(cpfValidator).isValido(null);
        verify(clienteGateway, never()).buscarPorCpf(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF vazio")
    void t5() throws Exception {
        String cpfVazio = "";
        
        when(cpfValidator.isValido(cpfVazio)).thenReturn(false);

        CpfInvalidoException exception = assertThrows(
                CpfInvalidoException.class,
                () -> autenticarCliente.executar(cpfVazio),
                "Deve lançar exceção para CPF vazio"
        );

        assertEquals("CPF inválido: ", exception.getMessage(),
                "Mensagem de erro deve estar correta");

        verify(cpfValidator).isValido(cpfVazio);
        verify(clienteGateway, never()).buscarPorCpf(anyString());
    }
}