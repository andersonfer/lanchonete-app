package br.com.lanchonete.clientes.adapters.web.service;

import br.com.lanchonete.clientes.adapters.web.dto.ClienteRequest;
import br.com.lanchonete.clientes.adapters.web.dto.ClienteResponse;
import br.com.lanchonete.clientes.application.usecases.BuscarClientePorCpf;
import br.com.lanchonete.clientes.application.usecases.CadastrarCliente;
import br.com.lanchonete.clientes.application.usecases.IdentificarCliente;
import br.com.lanchonete.clientes.domain.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private CadastrarCliente cadastrarCliente;

    @Mock
    private IdentificarCliente identificarCliente;

    @Mock
    private BuscarClientePorCpf buscarClientePorCpf;

    private ClienteService clienteService;

    private Cliente clienteMock;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(cadastrarCliente, identificarCliente, buscarClientePorCpf);
        clienteMock = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void t1() {
        ClienteRequest request = new ClienteRequest("João Silva", "joao@email.com", "12345678901");
        when(cadastrarCliente.executar(anyString(), anyString(), anyString())).thenReturn(clienteMock);

        ClienteResponse response = clienteService.cadastrar(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("João Silva", response.nome());
        assertEquals("joao@email.com", response.email());
        assertEquals("12345678901", response.cpf());
        verify(cadastrarCliente, times(1)).executar("João Silva", "joao@email.com", "12345678901");
    }

    @Test
    @DisplayName("Deve identificar cliente por CPF")
    void t2() {
        String cpf = "12345678901";
        when(identificarCliente.executar(cpf)).thenReturn(clienteMock);

        ClienteResponse response = clienteService.identificar(cpf);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("João Silva", response.nome());
        assertEquals("joao@email.com", response.email());
        assertEquals("12345678901", response.cpf());
        verify(identificarCliente, times(1)).executar(cpf);
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF")
    void t3() {
        String cpf = "12345678901";
        when(buscarClientePorCpf.executar(cpf)).thenReturn(clienteMock);

        ClienteResponse response = clienteService.buscarPorCpf(cpf);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("João Silva", response.nome());
        assertEquals("joao@email.com", response.email());
        assertEquals("12345678901", response.cpf());
        verify(buscarClientePorCpf, times(1)).executar(cpf);
    }
}
