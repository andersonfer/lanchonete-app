package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.EventPublisher;
import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.exceptions.PedidoNaoEncontradoException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RetirarPedidoUseCaseTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RetirarPedidoUseCase retirarPedidoUseCase;

    private Pedido pedidoPronto;
    private Pedido pedidoRetirado;

    @BeforeEach
    void configurar() {
        ItemPedido item = ItemPedido.criar(
                Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE),
                1
        );

        pedidoPronto = Pedido.criar(null, StatusPedido.PRONTO, LocalDateTime.now());
        pedidoPronto.setId(1L);
        pedidoPronto.adicionarItem(item);

        pedidoRetirado = Pedido.criar(null, StatusPedido.FINALIZADO, LocalDateTime.now());
        pedidoRetirado.setId(1L);
        pedidoRetirado.adicionarItem(item);
    }

    @Test
    @DisplayName("Deve retirar pedido com sucesso quando status é PRONTO")
    void t1() {
        when(pedidoGateway.buscarPorId(1L))
                .thenReturn(Optional.of(pedidoPronto))
                .thenReturn(Optional.of(pedidoRetirado));

        Pedido resultado = retirarPedidoUseCase.executar(1L);

        assertNotNull(resultado);
        assertEquals(StatusPedido.FINALIZADO, resultado.getStatus());
        verify(pedidoGateway, times(1)).atualizarStatus(1L, StatusPedido.FINALIZADO);
        verify(eventPublisher, times(1)).publicarPedidoRetirado(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existe")
    void t2() {
        when(pedidoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        PedidoNaoEncontradoException exception = assertThrows(PedidoNaoEncontradoException.class, () -> {
            retirarPedidoUseCase.executar(999L);
        });

        assertNotNull(exception);
        verify(pedidoGateway, never()).atualizarStatus(anyLong(), any());
        verify(eventPublisher, never()).publicarPedidoRetirado(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não está PRONTO")
    void t3() {
        Pedido pedidoEmPreparacao = Pedido.criar(null, StatusPedido.EM_PREPARACAO, LocalDateTime.now());
        pedidoEmPreparacao.setId(1L);
        pedidoEmPreparacao.adicionarItem(ItemPedido.criar(
                Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE),
                1
        ));

        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedidoEmPreparacao));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            retirarPedidoUseCase.executar(1L);
        });

        assertTrue(exception.getMessage().contains("Pedido não está pronto para retirada"));
        assertTrue(exception.getMessage().contains("EM_PREPARACAO"));
        verify(pedidoGateway, never()).atualizarStatus(anyLong(), any());
        verify(eventPublisher, never()).publicarPedidoRetirado(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido está CRIADO")
    void t4() {
        Pedido pedidoCriado = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        pedidoCriado.setId(1L);
        pedidoCriado.adicionarItem(ItemPedido.criar(
                Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE),
                1
        ));

        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedidoCriado));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            retirarPedidoUseCase.executar(1L);
        });

        assertTrue(exception.getMessage().contains("não está pronto para retirada"));
        verify(pedidoGateway, never()).atualizarStatus(anyLong(), any());
        verify(eventPublisher, never()).publicarPedidoRetirado(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido está CANCELADO")
    void t5() {
        Pedido pedidoCancelado = Pedido.criar(null, StatusPedido.CANCELADO, LocalDateTime.now());
        pedidoCancelado.setId(1L);
        pedidoCancelado.adicionarItem(ItemPedido.criar(
                Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE),
                1
        ));

        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedidoCancelado));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            retirarPedidoUseCase.executar(1L);
        });

        assertTrue(exception.getMessage().contains("não está pronto para retirada"));
        assertTrue(exception.getMessage().contains("CANCELADO"));
        verify(pedidoGateway, never()).atualizarStatus(anyLong(), any());
        verify(eventPublisher, never()).publicarPedidoRetirado(anyLong());
    }
}
