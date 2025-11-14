package br.com.lanchonete.pedidos.application.usecases;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BuscarPedidoUseCaseTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @InjectMocks
    private BuscarPedidoUseCase buscarPedidoUseCase;

    private Pedido pedido;

    @BeforeEach
    void configurar() {
        pedido = Pedido.criar(null, StatusPedido.CRIADO, LocalDateTime.now());
        pedido.setId(1L);
        pedido.adicionarItem(ItemPedido.criar(
                Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE),
                1
        ));
    }

    @Test
    @DisplayName("Deve buscar pedido por ID com sucesso")
    void t1() {
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedido));

        Pedido resultado = buscarPedidoUseCase.executar(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(pedidoGateway, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existe")
    void t2() {
        when(pedidoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        PedidoNaoEncontradoException exception = assertThrows(PedidoNaoEncontradoException.class, () -> {
            buscarPedidoUseCase.executar(999L);
        });

        assertNotNull(exception);
        verify(pedidoGateway, times(1)).buscarPorId(999L);
    }
}
