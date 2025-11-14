package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AtualizarStatusPedidoUseCaseTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @InjectMocks
    private AtualizarStatusPedidoUseCase atualizarStatusPedidoUseCase;

    @Test
    @DisplayName("Deve atualizar status do pedido")
    void t1() {
        Long pedidoId = 1L;
        StatusPedido novoStatus = StatusPedido.PRONTO;

        atualizarStatusPedidoUseCase.executar(pedidoId, novoStatus);

        verify(pedidoGateway, times(1)).atualizarStatus(pedidoId, novoStatus);
    }

    @Test
    @DisplayName("Deve atualizar status para EM_PREPARACAO")
    void t2() {
        Long pedidoId = 2L;
        StatusPedido novoStatus = StatusPedido.EM_PREPARACAO;

        atualizarStatusPedidoUseCase.executar(pedidoId, novoStatus);

        verify(pedidoGateway, times(1)).atualizarStatus(pedidoId, novoStatus);
    }
}
