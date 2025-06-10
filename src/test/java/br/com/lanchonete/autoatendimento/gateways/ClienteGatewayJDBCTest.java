package br.com.lanchonete.autoatendimento.gateways;

import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class ClienteGatewayJDBCTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClienteGatewayJDBC clienteRepositorio;

    private final String CPF_JA_CASTRADADO = "12345678901";

    private Cliente clientePreCadastrado;

    @BeforeEach
    void configurar() {
        clienteRepositorio = new ClienteGatewayJDBC(jdbcTemplate);

        clientePreCadastrado = Cliente.criar(
                "João Silva",
                "joao@email.com",
                CPF_JA_CASTRADADO
        );

        clienteRepositorio.salvar(clientePreCadastrado);
    }

    @Test
    @DisplayName("Deve encontrar o cliente por CPF")
    void t1() {

        Optional<Cliente> resultado = clienteRepositorio.buscarPorCpf(CPF_JA_CASTRADADO);

        assertTrue(resultado.isPresent(), "Cliente deve ser encontrado pelo CPF");
        Cliente cliente = resultado.get();
        assertEquals(CPF_JA_CASTRADADO, cliente.getCpf().getValor());
        assertEquals("João Silva", cliente.getNome());
        assertEquals("joao@email.com", cliente.getEmail());
    }

    @Test
    @DisplayName("Deve retornar vazio quando CPF não existe")
    void t2() {
        String cpfInexistente = "99999999999";

        Optional<Cliente> resultado = clienteRepositorio.buscarPorCpf(cpfInexistente);

        assertFalse(resultado.isPresent(), "Não deve encontrar cliente com CPF inexistente");
        assertTrue(resultado.isEmpty(), "O resultado deve ser vazio");
    }

    @Test
    @DisplayName("Deve encontrar cliente após salvar")
    void t3() {
        String cpf = "23456789012";

        Cliente novoCliente = Cliente.criar(
                "Maria Souza",
                "maria@email.com",
                cpf
        );

        Cliente clienteSalvo = clienteRepositorio.salvar(novoCliente);
        Optional<Cliente> clienteEncontrado = clienteRepositorio.buscarPorCpf(cpf);

        assertNotNull(clienteSalvo.getId(), "ID do cliente deve ser preenchido");
        assertTrue(clienteEncontrado.isPresent(), "Cliente deve ser encontrado pelo CPF");
        assertEquals(novoCliente, clienteEncontrado.get(), "Cliente salvo deve ser igual ao cliente encontrado pelo CPF");
    }

    @Test
    @DisplayName("Não deve permitir inserção de cpf duplicado")
    void t4() {
        Cliente clienteComCpfDuplicado = Cliente.criar(
                "Pedro Santos",
                "pedro@email.com",
                CPF_JA_CASTRADADO
        );

        assertThrows(DataIntegrityViolationException.class, () -> clienteRepositorio.salvar(clienteComCpfDuplicado));
    }

    @Test
    @DisplayName("Deve encontrar cliente por ID")
    void t5() {
        // Utiliza o cliente pré-cadastrado no @BeforeEach
        Optional<Cliente> resultado = clienteRepositorio.buscarPorId(clientePreCadastrado.getId());

        assertTrue(resultado.isPresent(), "Cliente deve ser encontrado pelo ID");
        assertEquals(clientePreCadastrado.getNome(), resultado.get().getNome());
        assertEquals(clientePreCadastrado.getEmail(), resultado.get().getEmail());
        assertEquals(clientePreCadastrado.getCpf(), resultado.get().getCpf());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar ID inexistente")
    void t6() {
        Optional<Cliente> resultado = clienteRepositorio.buscarPorId(999L);
        assertTrue(resultado.isEmpty(), "Não deve encontrar cliente com ID inexistente");
    }

}