package br.com.lanchonete.cozinha.adapters.web.controller;

import br.com.lanchonete.cozinha.adapters.messaging.publisher.PedidoProntoPublisher;
import br.com.lanchonete.cozinha.adapters.web.dto.PedidoCozinhaResponse;
import br.com.lanchonete.cozinha.application.usecases.IniciarPreparoPedidoUseCase;
import br.com.lanchonete.cozinha.application.usecases.ListarPedidosCozinhaUseCase;
import br.com.lanchonete.cozinha.application.usecases.MarcarPedidoComoProntoUseCase;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CozinhaControllerTest {

    @Mock
    private ListarPedidosCozinhaUseCase listarPedidosCozinhaUseCase;

    @Mock
    private IniciarPreparoPedidoUseCase iniciarPreparoPedidoUseCase;

    @Mock
    private MarcarPedidoComoProntoUseCase marcarPedidoComoProntoUseCase;

    @Mock
    private PedidoProntoPublisher pedidoProntoPublisher;

    private CozinhaController cozinhaController;

    private PedidoCozinha pedidoMock;

    @BeforeEach
    void setUp() {
        cozinhaController = new CozinhaController(
            listarPedidosCozinhaUseCase,
            iniciarPreparoPedidoUseCase,
            marcarPedidoComoProntoUseCase,
            pedidoProntoPublisher
        );

        pedidoMock = new PedidoCozinha(1L);
        pedidoMock.setId(1L);
        pedidoMock.setStatus(StatusPedido.AGUARDANDO);
        pedidoMock.setDataInicio(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve listar fila da cozinha e retornar status 200 OK")
    void t1() {
        PedidoCozinha pedido1 = new PedidoCozinha(1L);
        pedido1.setId(1L);
        pedido1.setStatus(StatusPedido.AGUARDANDO);

        PedidoCozinha pedido2 = new PedidoCozinha(2L);
        pedido2.setId(2L);
        pedido2.setStatus(StatusPedido.EM_PREPARO);

        when(listarPedidosCozinhaUseCase.executar()).thenReturn(Arrays.asList(pedido1, pedido2));

        ResponseEntity<List<PedidoCozinhaResponse>> response = cozinhaController.listarFila();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(2L, response.getBody().get(1).getId());
        verify(listarPedidosCozinhaUseCase, times(1)).executar();
    }

    @Test
    @DisplayName("Deve iniciar preparo de pedido e retornar status 200 OK")
    void t2() {
        pedidoMock.setStatus(StatusPedido.EM_PREPARO);
        when(iniciarPreparoPedidoUseCase.executar(1L)).thenReturn(pedidoMock);

        ResponseEntity<PedidoCozinhaResponse> response = cozinhaController.iniciarPreparo(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(StatusPedido.EM_PREPARO, response.getBody().getStatus());
        verify(iniciarPreparoPedidoUseCase, times(1)).executar(1L);
    }

    @Test
    @DisplayName("Deve marcar pedido como pronto, publicar evento e retornar status 200 OK")
    void t3() {
        pedidoMock.setStatus(StatusPedido.PRONTO);
        pedidoMock.setDataFim(LocalDateTime.now());
        when(marcarPedidoComoProntoUseCase.executar(1L)).thenReturn(pedidoMock);
        doNothing().when(pedidoProntoPublisher).publicar(1L);

        ResponseEntity<PedidoCozinhaResponse> response = cozinhaController.marcarComoPronto(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(StatusPedido.PRONTO, response.getBody().getStatus());
        assertNotNull(response.getBody().getDataFim());
        verify(marcarPedidoComoProntoUseCase, times(1)).executar(1L);
        verify(pedidoProntoPublisher, times(1)).publicar(1L);
    }
}
