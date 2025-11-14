package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ListarPedidosUseCaseTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @InjectMocks
    private ListarPedidosUseCase listarPedidosUseCase;

    private Pedido pedido1;
    private Pedido pedido2;

    @BeforeEach
    void configurar() {
        pedido1 = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        pedido1.setId(1L);

        pedido2 = Pedido.criar(null, StatusPedido.PRONTO, LocalDateTime.now());
        pedido2.setId(2L);
    }

    @Test
    @DisplayName("Deve listar todos os pedidos quando status é nulo")
    void t1() {
        List<Pedido> todosPedidos = Arrays.asList(pedido1, pedido2);
        when(pedidoGateway.listarTodos()).thenReturn(todosPedidos);

        List<Pedido> resultado = listarPedidosUseCase.executar(null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pedidoGateway, times(1)).listarTodos();
        verify(pedidoGateway, never()).listarPorStatus(any());
    }

    @Test
    @DisplayName("Deve listar pedidos filtrados por status CRIADO")
    void t2() {
        List<Pedido> pedidosCriados = Arrays.asList(pedido1);
        when(pedidoGateway.listarPorStatus(StatusPedido.CRIADO)).thenReturn(pedidosCriados);

        List<Pedido> resultado = listarPedidosUseCase.executar(StatusPedido.CRIADO);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(StatusPedido.CRIADO, resultado.get(0).getStatus());
        verify(pedidoGateway, times(1)).listarPorStatus(StatusPedido.CRIADO);
        verify(pedidoGateway, never()).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void t3() {
        when(pedidoGateway.listarTodos()).thenReturn(List.of());

        List<Pedido> resultado = listarPedidosUseCase.executar(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoGateway, times(1)).listarTodos();
    }
}
