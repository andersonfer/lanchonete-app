package br.com.lanchonete.pedidos.adapters.external;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteGatewayFeignTest {

    @Mock
    private ClienteFeignClient feignClient;

    private ClienteGatewayFeign clienteGateway;

    @BeforeEach
    void setUp() {
        clienteGateway = new ClienteGatewayFeign(feignClient);
    }

    @Test
    @DisplayName("Deve retornar true quando cliente existe")
    void t1() {
        String cpf = "12345678901";
        ClienteResponse mockResponse = new ClienteResponse(1L, cpf, "João Silva", "joao@email.com");
        when(feignClient.buscarPorCpf(cpf)).thenReturn(mockResponse);

        boolean existe = clienteGateway.existeCliente(cpf);

        assertTrue(existe);
        verify(feignClient, times(1)).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve retornar false quando cliente não existe (FeignException.NotFound)")
    void t2() {
        String cpf = "99999999999";
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(feignClient.buscarPorCpf(cpf)).thenThrow(notFound);

        boolean existe = clienteGateway.existeCliente(cpf);

        assertFalse(existe);
        verify(feignClient, times(1)).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando ocorre outro erro Feign")
    void t3() {
        String cpf = "12345678901";
        FeignException feignException = mock(FeignException.class);
        when(feignException.getMessage()).thenReturn("Service unavailable");
        when(feignClient.buscarPorCpf(cpf)).thenThrow(feignException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clienteGateway.existeCliente(cpf);
        });

        assertTrue(exception.getMessage().contains("Erro ao validar cliente"));
        verify(feignClient, times(1)).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve buscar nome do cliente com sucesso")
    void t4() {
        String cpf = "12345678901";
        ClienteResponse mockResponse = new ClienteResponse(1L, cpf, "Maria Souza", "maria@email.com");
        when(feignClient.buscarPorCpf(cpf)).thenReturn(mockResponse);

        String nome = clienteGateway.buscarNomeCliente(cpf);

        assertEquals("Maria Souza", nome);
        verify(feignClient, times(1)).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve retornar null quando ocorre FeignException ao buscar nome")
    void t5() {
        String cpf = "99999999999";
        FeignException feignException = mock(FeignException.class);
        when(feignClient.buscarPorCpf(cpf)).thenThrow(feignException);

        String nome = clienteGateway.buscarNomeCliente(cpf);

        assertNull(nome);
        verify(feignClient, times(1)).buscarPorCpf(cpf);
    }

    @Test
    @DisplayName("Deve retornar null quando cliente não tem nome")
    void t6() {
        String cpf = "12345678901";
        ClienteResponse mockResponse = new ClienteResponse(1L, cpf, null, "email@test.com");
        when(feignClient.buscarPorCpf(cpf)).thenReturn(mockResponse);

        String nome = clienteGateway.buscarNomeCliente(cpf);

        assertNull(nome);
        verify(feignClient, times(1)).buscarPorCpf(cpf);
    }
}
