package br.com.lanchonete.clientes.application.usecases;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.clientes.domain.model.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BuscarClientePorCpfTest {

    @Mock
    private ClienteGateway clienteGateway;

    @InjectMocks
    private BuscarClientePorCpf buscarClientePorCpf;

    @Test
    @DisplayName("Deve retornar Cliente ao encontrar cliente pelo CPF")
    void t1() {
        String cpf = "12345678910";
        Cliente clienteEsperado = Cliente.reconstituir(
                1L,
                "Pedro Santos",
                "pedro.santos@email.com",
                cpf
        );
        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.of(clienteEsperado));

        Cliente clienteRetornado = buscarClientePorCpf.executar(cpf);

        assertNotNull(clienteRetornado);
        assertEquals(clienteEsperado.getId(), clienteRetornado.getId());
        assertEquals(clienteEsperado.getNome(), clienteRetornado.getNome());
        assertEquals(clienteEsperado.getEmail().getValor(), clienteRetornado.getEmail().getValor());
        assertEquals(clienteEsperado.getCpf().getValor(), clienteRetornado.getCpf().getValor());
    }

    @Test
    @DisplayName("Deve lançar ClienteNaoEncontradoException ao informar CPF inexistente")
    void t2() {
        String cpf = "12345678910";

        when(clienteGateway.buscarPorCpf(cpf)).thenReturn(Optional.empty());

        ClienteNaoEncontradoException excecao = assertThrows(ClienteNaoEncontradoException.class,
                () -> buscarClientePorCpf.executar(cpf));

        assertTrue(excecao.getMessage().contains(cpf));
    }
}
