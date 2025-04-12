package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CadastrarClienteServiceTest {

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @InjectMocks
    private CadastrarClienteService cadastrarClienteService;

    private CadastrarClienteDTO clienteValido;
    private Cliente clienteSalvo;

    @BeforeEach
    void configurar() {
        clienteValido = CadastrarClienteDTO.builder()
                .nome("João Silva")
                .email("joao.silva@example.com")
                .cpf("12345678901")
                .build();

        clienteSalvo = Cliente.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao.silva@example.com")
                .cpf("12345678901")
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso quando os dados são válidos")
    void t1() {

        when(clienteRepositorio.salvar(any(Cliente.class))).thenReturn(clienteSalvo);

        ClienteResponseDTO response = cadastrarClienteService.cadastrar(clienteValido);

        assertNotNull(response, "A resposta não deveria ser nula.");
        assertEquals(1L, response.getId(), "O ID do cliente salvo deveria ser 1.");
        assertEquals("João Silva", response.getNome(), "O nome do cliente salvo está incorreto.");
        assertEquals("joao.silva@example.com", response.getEmail(), "O email do cliente salvo está incorreto.");
        assertEquals("12345678901", response.getCpf(), "O CPF do cliente salvo está incorreto.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com nome vazio")
    void t2() {
        CadastrarClienteDTO dto = CadastrarClienteDTO.builder()
                .nome("")
                .email("joao.silva@example.com")
                .cpf("12345678901")
                .build();

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarClienteService.cadastrar(dto),
                "Deveria lançar uma exceção para nome vazio.");

        assertEquals("Nome é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email vazio")
    void t3() {
        CadastrarClienteDTO dto = CadastrarClienteDTO.builder()
                .nome("João Silva")
                .email("")
                .cpf("12345678901")
                .build();

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarClienteService.cadastrar(dto),
                "Deveria lançar uma exceção para email vazio.");

        assertEquals("Email é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email inválido")
    void t4() {
        CadastrarClienteDTO dto = CadastrarClienteDTO.builder()
                .nome("João Silva")
                .email("email_invalido")
                .cpf("12345678901")
                .build();

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarClienteService.cadastrar(dto),
                "Deveria lançar uma exceção para email inválido.");

        assertEquals("Email inválido", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF vazio")
    void t5() {
        CadastrarClienteDTO dto = CadastrarClienteDTO.builder()
                .nome("João Silva")
                .email("joao.silva@example.com")
                .cpf("")
                .build();

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarClienteService.cadastrar(dto),
                "Deveria lançar uma exceção para CPF vazio.");

        assertEquals("CPF é obrigatório", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF inválido")
    void t6() {
        CadastrarClienteDTO dto = CadastrarClienteDTO.builder()
                .nome("João Silva")
                .email("joao.silva@example.com")
                .cpf("12345")
                .build();

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarClienteService.cadastrar(dto),
                "Deveria lançar uma exceção para CPF inválido.");

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage(), "Mensagem da exceção está incorreta.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já existe")
    void t7() {

        when(clienteRepositorio.buscarPorCpf(clienteValido.getCpf())).thenReturn(Optional.of(clienteSalvo));

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> cadastrarClienteService.cadastrar(clienteValido));

        assertTrue(ex.getMessage().equals("CPF duplicado"));

        verify(clienteRepositorio, never()).salvar(any(Cliente.class));
    }
}