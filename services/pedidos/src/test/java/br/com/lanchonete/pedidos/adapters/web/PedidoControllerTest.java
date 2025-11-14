package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.adapters.web.dto.ItemPedidoRequest;
import br.com.lanchonete.pedidos.adapters.web.dto.PedidoResponse;
import br.com.lanchonete.pedidos.adapters.web.dto.RealizarPedidoRequest;
import br.com.lanchonete.pedidos.application.usecases.BuscarPedidoUseCase;
import br.com.lanchonete.pedidos.application.usecases.CriarPedidoUseCase;
import br.com.lanchonete.pedidos.application.usecases.ListarPedidosUseCase;
import br.com.lanchonete.pedidos.application.usecases.RetirarPedidoUseCase;
import br.com.lanchonete.pedidos.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private CriarPedidoUseCase criarPedidoUseCase;

    @Mock
    private BuscarPedidoUseCase buscarPedidoUseCase;

    @Mock
    private ListarPedidosUseCase listarPedidosUseCase;

    @Mock
    private RetirarPedidoUseCase retirarPedidoUseCase;

    private PedidoController pedidoController;

    private Pedido pedido1;
    private Pedido pedido2;
    private Produto produto1;

    @BeforeEach
    void setUp() {
        pedidoController = new PedidoController(
                criarPedidoUseCase,
                buscarPedidoUseCase,
                listarPedidosUseCase,
                retirarPedidoUseCase
        );

        produto1 = Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE);

        pedido1 = Pedido.criar("12345678901", StatusPedido.CRIADO, LocalDateTime.now());
        pedido1.setId(1L);
        pedido1.setClienteNome("João Silva");
        pedido1.adicionarItem(ItemPedido.criar(produto1, 2));

        pedido2 = Pedido.criar(null, StatusPedido.REALIZADO, LocalDateTime.now());
        pedido2.setId(2L);
        pedido2.adicionarItem(ItemPedido.criar(produto1, 1));
    }

    @Test
    @DisplayName("Deve realizar pedido e retornar status 201 CREATED")
    void t1() {
        ItemPedidoRequest itemRequest = new ItemPedidoRequest(1L, 2);
        RealizarPedidoRequest request = new RealizarPedidoRequest("12345678901", Arrays.asList(itemRequest));

        when(criarPedidoUseCase.executar(anyString(), any())).thenReturn(pedido1);

        ResponseEntity<PedidoResponse> response = pedidoController.realizarPedido(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("12345678901", response.getBody().getCpfCliente());
        assertEquals("João Silva", response.getBody().getClienteNome());
        assertEquals(StatusPedido.CRIADO, response.getBody().getStatus());
        verify(criarPedidoUseCase, times(1)).executar(anyString(), any());
    }

    @Test
    @DisplayName("Deve realizar pedido anônimo e retornar status 201 CREATED")
    void t2() {
        ItemPedidoRequest itemRequest = new ItemPedidoRequest(1L, 1);
        RealizarPedidoRequest request = new RealizarPedidoRequest(null, Arrays.asList(itemRequest));

        when(criarPedidoUseCase.executar(any(), any())).thenReturn(pedido2);

        ResponseEntity<PedidoResponse> response = pedidoController.realizarPedido(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
        assertNull(response.getBody().getCpfCliente());
        assertEquals(StatusPedido.REALIZADO, response.getBody().getStatus());
        verify(criarPedidoUseCase, times(1)).executar(any(), any());
    }

    @Test
    @DisplayName("Deve buscar pedido por ID e retornar status 200 OK")
    void t3() {
        when(buscarPedidoUseCase.executar(1L)).thenReturn(pedido1);

        ResponseEntity<PedidoResponse> response = pedidoController.buscarPedido(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("12345678901", response.getBody().getCpfCliente());
        verify(buscarPedidoUseCase, times(1)).executar(1L);
    }

    @Test
    @DisplayName("Deve listar todos os pedidos e retornar status 200 OK")
    void t4() {
        List<Pedido> pedidos = Arrays.asList(pedido1, pedido2);
        when(listarPedidosUseCase.executar(null)).thenReturn(pedidos);

        ResponseEntity<List<PedidoResponse>> response = pedidoController.listarPedidos(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(2L, response.getBody().get(1).getId());
        verify(listarPedidosUseCase, times(1)).executar(null);
    }

    @Test
    @DisplayName("Deve listar pedidos por status e retornar status 200 OK")
    void t5() {
        List<Pedido> pedidos = Arrays.asList(pedido1);
        when(listarPedidosUseCase.executar(StatusPedido.CRIADO)).thenReturn(pedidos);

        ResponseEntity<List<PedidoResponse>> response = pedidoController.listarPedidos(StatusPedido.CRIADO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(StatusPedido.CRIADO, response.getBody().get(0).getStatus());
        verify(listarPedidosUseCase, times(1)).executar(StatusPedido.CRIADO);
    }

    @Test
    @DisplayName("Deve retirar pedido e retornar status 200 OK")
    void t6() {
        pedido1.setStatus(StatusPedido.FINALIZADO);
        when(retirarPedidoUseCase.executar(1L)).thenReturn(pedido1);

        ResponseEntity<PedidoResponse> response = pedidoController.retirarPedido(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(StatusPedido.FINALIZADO, response.getBody().getStatus());
        verify(retirarPedidoUseCase, times(1)).executar(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void t7() {
        when(listarPedidosUseCase.executar(any())).thenReturn(Arrays.asList());

        ResponseEntity<List<PedidoResponse>> response = pedidoController.listarPedidos(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(listarPedidosUseCase, times(1)).executar(null);
    }
}
