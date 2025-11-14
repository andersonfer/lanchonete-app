package br.com.lanchonete.clientes.adapters.web.controller;

import br.com.lanchonete.clientes.adapters.web.dto.ClienteRequest;
import br.com.lanchonete.clientes.adapters.web.dto.ClienteResponse;
import br.com.lanchonete.clientes.adapters.web.dto.IdentificarClienteRequest;
import br.com.lanchonete.clientes.adapters.web.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    private ClienteController clienteController;

    private ClienteResponse clienteResponseMock;

    @BeforeEach
    void setUp() {
        clienteController = new ClienteController(clienteService);
        clienteResponseMock = new ClienteResponse(1L, "João Silva", "joao@email.com", "12345678901");
    }

    @Test
    @DisplayName("Deve cadastrar cliente e retornar status 201 CREATED")
    void t1() {
        ClienteRequest request = new ClienteRequest("João Silva", "joao@email.com", "12345678901");
        when(clienteService.cadastrar(any(ClienteRequest.class))).thenReturn(clienteResponseMock);

        ResponseEntity<ClienteResponse> response = clienteController.cadastrar(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("João Silva", response.getBody().nome());
        assertEquals("joao@email.com", response.getBody().email());
        assertEquals("12345678901", response.getBody().cpf());
        verify(clienteService, times(1)).cadastrar(request);
    }

    @Test
    @DisplayName("Deve identificar cliente e retornar status 200 OK")
    void t2() {
        IdentificarClienteRequest request = new IdentificarClienteRequest("12345678901");
        when(clienteService.identificar(anyString())).thenReturn(clienteResponseMock);

        ResponseEntity<ClienteResponse> response = clienteController.identificar(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("João Silva", response.getBody().nome());
        verify(clienteService, times(1)).identificar("12345678901");
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF e retornar status 200 OK")
    void t3() {
        String cpf = "12345678901";
        when(clienteService.buscarPorCpf(anyString())).thenReturn(clienteResponseMock);

        ResponseEntity<ClienteResponse> response = clienteController.buscarPorCpf(cpf);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("João Silva", response.getBody().nome());
        assertEquals("joao@email.com", response.getBody().email());
        assertEquals("12345678901", response.getBody().cpf());
        verify(clienteService, times(1)).buscarPorCpf(cpf);
    }
}
