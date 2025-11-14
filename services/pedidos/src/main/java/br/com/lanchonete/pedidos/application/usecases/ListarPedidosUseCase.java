package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;

import java.util.List;

public class ListarPedidosUseCase {

    private final PedidoGateway pedidoGateway;

    public ListarPedidosUseCase(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public List<Pedido> executar(StatusPedido status) {
        if (status != null) {
            return pedidoGateway.listarPorStatus(status);
        }
        return pedidoGateway.listarTodos();
    }
}
