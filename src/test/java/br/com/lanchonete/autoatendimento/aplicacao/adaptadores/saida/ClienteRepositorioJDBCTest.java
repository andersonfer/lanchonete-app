package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.saida;

import br.com.lanchonete.autoatendimento.dominio.Cliente;
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
class ClienteRepositorioJDBCTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClienteRepositorioJDBC clienteRepositorio;

    @BeforeEach
    void configurar() {
        clienteRepositorio = new ClienteRepositorioJDBC(jdbcTemplate);
    }

    @Test
    @DisplayName("Deve encontrar o cliente por CPF")
    void t1() {
        String cpfExistente = "12345678901";

        Optional<Cliente> resultado = clienteRepositorio.buscarPorCpf(cpfExistente);

        assertTrue(resultado.isPresent(), "Cliente deve ser encontrado pelo CPF");
        Cliente cliente = resultado.get();
        assertEquals(cpfExistente, cliente.getCpf());
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

        Cliente novoCliente = Cliente.builder()
                .nome("Maria Souza")
                .email("maria@email.com")
                .cpf(cpf)
                .build();

        Cliente clienteSalvo = clienteRepositorio.salvar(novoCliente);
        Optional<Cliente> clienteEncontrado = clienteRepositorio.buscarPorCpf(cpf);

        assertNotNull(clienteSalvo.getId(), "ID do cliente deve ser preenchido");
        assertTrue(clienteEncontrado.isPresent(), "Cliente deve ser encontrado pelo CPF");
        assertEquals(novoCliente, clienteEncontrado.get(), "Cliente salvo deve ser igual ao cliente encontrado pelo CPF");
    }

    @Test
    @DisplayName("Não deve permitir inserção de cpf duplicado")
    void t4() {
        Cliente clienteComCpfDuplicado = Cliente.builder()
                .nome("Pedro Santos")
                .email("pedro@email.com")
                .cpf("12345678901")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> clienteRepositorio.salvar(clienteComCpfDuplicado));
    }

}