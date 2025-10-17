package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.exceptions.PedidoNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.model.Pedido;

public class BuscarPedidoUseCase {

    private final PedidoGateway pedidoGateway;

    public BuscarPedidoUseCase(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public Pedido executar(Long id) {
        return pedidoGateway.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
}
