package br.com.lanchonete.clientes.application.usecases;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.exceptions.ValidacaoException;
import br.com.lanchonete.clientes.domain.model.Cliente;
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
        when(clienteGateway.buscarPorCpf(cpfValido)).thenReturn(Optional.empty());
        when(clienteGateway.salvar(any(Cliente.class))).thenReturn(clienteSalvo);

        Cliente clienteRetornado = cadastrarCliente.executar(nomeValido, emailValido, cpfValido);

        assertNotNull(clienteRetornado);
        assertEquals(1L, clienteRetornado.getId());
        assertEquals("João Silva", clienteRetornado.getNome());
        assertEquals("joao.silva@example.com", clienteRetornado.getEmail().getValor());
        assertEquals("12345678901", clienteRetornado.getCpf().getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com nome vazio")
    void t2() {
        String nomeVazio = "";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeVazio, emailValido, cpfValido));

        assertEquals("Nome é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email vazio")
    void t3() {
        String emailVazio = "";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailVazio, cpfValido));

        assertEquals("Email é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com email inválido")
    void t4() {
        String emailInvalido = "email_invalido";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailInvalido, cpfValido));

        assertEquals("Email inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF vazio")
    void t5() {
        String cpfVazio = "";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailValido, cpfVazio));

        assertEquals("CPF é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar cliente com CPF inválido")
    void t6() {
        String cpfInvalido = "12345";

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailValido, cpfInvalido));

        assertEquals("CPF deve conter 11 dígitos numéricos", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já existe")
    void t7() {
        when(clienteGateway.buscarPorCpf(cpfValido)).thenReturn(Optional.of(clienteSalvo));

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> cadastrarCliente.executar(nomeValido, emailValido, cpfValido));

        assertEquals("CPF já cadastrado", ex.getMessage());

        verify(clienteGateway, never()).salvar(any(Cliente.class));
    }
}
