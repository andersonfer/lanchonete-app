package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;

public class AtualizarStatusPedidoUseCase {

    private final PedidoGateway pedidoGateway;

    public AtualizarStatusPedidoUseCase(PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public void executar(Long pedidoId, StatusPedido novoStatus) {
        pedidoGateway.atualizarStatus(pedidoId, novoStatus);
    }
}
