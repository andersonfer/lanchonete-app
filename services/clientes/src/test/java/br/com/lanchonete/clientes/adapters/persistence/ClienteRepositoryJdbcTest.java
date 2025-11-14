package br.com.lanchonete.clientes.adapters.persistence;

import br.com.lanchonete.clientes.domain.model.Cliente;
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
class ClienteRepositoryJdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClienteRepositoryJdbc clienteRepositorio;

    private final String CPF_JA_CADASTRADO = "12345678901";

    private Cliente clientePreCadastrado;

    @BeforeEach
    void configurar() {
        clienteRepositorio = new ClienteRepositoryJdbc(jdbcTemplate);

        Cliente novoCliente = Cliente.criar(
                "João Silva",
                "joao@email.com",
                CPF_JA_CADASTRADO
        );

        clientePreCadastrado = clienteRepositorio.salvar(novoCliente);
    }

    @Test
    @DisplayName("Deve encontrar o cliente por CPF")
    void t1() {
        Optional<Cliente> resultado = clienteRepositorio.buscarPorCpf(CPF_JA_CADASTRADO);

        assertTrue(resultado.isPresent());
        Cliente cliente = resultado.get();
        assertEquals(CPF_JA_CADASTRADO, cliente.getCpf().getValor());
        assertEquals("João Silva", cliente.getNome());
        assertEquals("joao@email.com", cliente.getEmail().getValor());
    }

    @Test
    @DisplayName("Deve retornar vazio quando CPF não existe")
    void t2() {
        String cpfInexistente = "99999999999";

        Optional<Cliente> resultado = clienteRepositorio.buscarPorCpf(cpfInexistente);

        assertFalse(resultado.isPresent());
        assertTrue(resultado.isEmpty());
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

        assertNotNull(clienteSalvo.getId());
        assertTrue(clienteEncontrado.isPresent());
        Cliente encontrado = clienteEncontrado.get();
        assertEquals(clienteSalvo.getId(), encontrado.getId());
        assertEquals(clienteSalvo.getNome(), encontrado.getNome());
        assertEquals(clienteSalvo.getEmail().getValor(), encontrado.getEmail().getValor());
        assertEquals(clienteSalvo.getCpf().getValor(), encontrado.getCpf().getValor());
    }

    @Test
    @DisplayName("Não deve permitir inserção de cpf duplicado")
    void t4() {
        Cliente clienteComCpfDuplicado = Cliente.criar(
                "Pedro Santos",
                "pedro@email.com",
                CPF_JA_CADASTRADO
        );

        assertThrows(DataIntegrityViolationException.class, () -> clienteRepositorio.salvar(clienteComCpfDuplicado));
    }

    @Test
    @DisplayName("Deve encontrar cliente por ID")
    void t5() {
        Optional<Cliente> resultado = clienteRepositorio.buscarPorId(clientePreCadastrado.getId());

        assertTrue(resultado.isPresent());
        Cliente encontrado = resultado.get();
        assertEquals(clientePreCadastrado.getNome(), encontrado.getNome());
        assertEquals(clientePreCadastrado.getEmail().getValor(), encontrado.getEmail().getValor());
        assertEquals(clientePreCadastrado.getCpf().getValor(), encontrado.getCpf().getValor());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar ID inexistente")
    void t6() {
        Optional<Cliente> resultado = clienteRepositorio.buscarPorId(999L);
        assertTrue(resultado.isEmpty());
    }
}
