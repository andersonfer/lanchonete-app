package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CadastrarClienteTest {

    @Mock
    private ClienteGateway clienteGateway;

    @InjectMocks
    private CadastrarCliente cadastrarCliente;

    private String nomeValido;
    private String cpfValido;
    private String emailValido;
    private Cliente clienteSalvo;

    @BeforeEach
    void configurar() {
        nomeValido = "João Silva";
        cpfValido = "12345678901";
        emailValido = "joao.silva@example.com";

        clienteSalvo = Cliente.reconstituir(
                1L,
                "João Silva",
                "joao.silva@example.com",
                "12345678901"
        );
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso quando os dados são válidos")
    void t1() {

        when(clienteGateway.salvar(any(Cliente.class))).thenReturn(clienteSalvo);

        Cliente clienteRetornado = cadastrarCliente.executar(nomeValido, emailValido, cpfValido);

        assertNotNull(clienteRetornado, "O cliente retornado não deveria ser nulo.");
        assertEquals(1L, clienteRetornado.getId(), "O ID do cliente salvo deveria ser 1.");
        assertEquals("João Silva", clienteRetornado.getNome(), "O nome do cliente salvo está incorreto.");
        assertEquals("joao.silva@example.com", clienteRetornado.getEmail().getValor(), "O email do cliente salvo está incorreto.");
        assertEquals("12345678901", clienteRetornado.getCpf().getValor(), "O CPF do cliente salvo está incorreto.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com nome vazio")
    void t2() {
        String nomeVazio = "";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeVazio, emailValido, cpfValido),
                "Deveria lançar uma exceção para nome vazio.");

        assertEquals("Nome é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email vazio")
    void t3() {
        String emailVazio = "";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailVazio, cpfValido),
                "Deveria lançar uma exceção para email vazio.");

        assertEquals("Email é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email inválido")
    void t4() {
        String emailInvalido = "email_invalido";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailInvalido, cpfValido),
                "Deveria lançar uma exceção para email inválido.");

        assertEquals("Email inválido", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF vazio")
    void t5() {
        String cpfVazio = "";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailValido, cpfVazio),
                "Deveria lançar uma exceção para CPF vazio.");

        assertEquals("CPF é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF inválido")
    void t6() {
        String cpfInvalido = "12345";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailValido, cpfInvalido),
                "Deveria lançar uma exceção para CPF inválido.");

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já existe")
    void t7() {

        when(clienteGateway.buscarPorCpf(cpfValido)).thenReturn(Optional.of(clienteSalvo));

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailValido, cpfValido));

        assertEquals("CPF duplicado",ex.getMessage());

        verify(clienteGateway, never()).salvar(any(Cliente.class));
    }
}