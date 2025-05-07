package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.modelo.Cliente;
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
    private ClienteRepositorio clienteRepositorio;

    @InjectMocks
    private CadastrarCliente cadastrarCliente;

    private ClienteRequestDTO clienteValido;
    private Cliente clienteSalvo;

    @BeforeEach
    void configurar() {

        clienteValido = new ClienteRequestDTO("João Silva", "12345678901", "Joao.silva@example.com");

        clienteSalvo = Cliente.criarSemValidacao(
                1L,
                "João Silva",
                "joao.silva@example.com",
                "12345678901"
        );
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso quando os dados são válidos")
    void t1() {

        when(clienteRepositorio.salvar(any(Cliente.class))).thenReturn(clienteSalvo);

        ClienteResponseDTO response = cadastrarCliente.executar(clienteValido);

        assertNotNull(response, "A resposta não deveria ser nula.");
        assertEquals(1L, response.id(), "O ID do cliente salvo deveria ser 1.");
        assertEquals("João Silva", response.nome(), "O nome do cliente salvo está incorreto.");
        assertEquals("joao.silva@example.com", response.email(), "O email do cliente salvo está incorreto.");
        assertEquals("12345678901", response.cpf(), "O CPF do cliente salvo está incorreto.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com nome vazio")
    void t2() {
        ClienteRequestDTO novoCliente = new ClienteRequestDTO("", "12345678901", "Joao.silva@example.com");

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(novoCliente),
                "Deveria lançar uma exceção para nome vazio.");

        assertEquals("Nome é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email vazio")
    void t3() {
        ClienteRequestDTO novoCliente = new ClienteRequestDTO("João Silva", "12345678901", "");

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(novoCliente),
                "Deveria lançar uma exceção para email vazio.");

        assertEquals("Email é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email inválido")
    void t4() {
        ClienteRequestDTO novoCliente = new ClienteRequestDTO("João Silva", "12345678901", "email_invalido");

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(novoCliente),
                "Deveria lançar uma exceção para email inválido.");

        assertEquals("Email inválido", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF vazio")
    void t5() {
        ClienteRequestDTO novoCliente = new ClienteRequestDTO("João Silva", "", "Joao.silva@example.com");

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(novoCliente),
                "Deveria lançar uma exceção para CPF vazio.");

        assertEquals("CPF é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF inválido")
    void t6() {
        ClienteRequestDTO novoCliente = new ClienteRequestDTO("João Silva", "12345", "Joao.silva@example.com");

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(novoCliente),
                "Deveria lançar uma exceção para CPF inválido.");

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já existe")
    void t7() {

        when(clienteRepositorio.buscarPorCpf(clienteValido.cpf())).thenReturn(Optional.of(clienteSalvo));

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(clienteValido));

        assertEquals("CPF duplicado",ex.getMessage());

        verify(clienteRepositorio, never()).salvar(any(Cliente.class));
    }
}