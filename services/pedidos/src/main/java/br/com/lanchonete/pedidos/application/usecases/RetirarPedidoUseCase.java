package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.EventPublisher;
import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.domain.exceptions.PedidoNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;

public class RetirarPedidoUseCase {

    private final PedidoGateway pedidoGateway;
    private final EventPublisher eventPublisher;

    public RetirarPedidoUseCase(PedidoGateway pedidoGateway, EventPublisher eventPublisher) {
        this.pedidoGateway = pedidoGateway;
        this.eventPublisher = eventPublisher;
    }

    public Pedido executar(Long id) {
        Pedido pedido = pedidoGateway.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));

        // Validar se pedido está pronto para retirada
        if (pedido.getStatus() != StatusPedido.PRONTO) {
            throw new IllegalArgumentException("Pedido não está pronto para retirada. Status atual: " + pedido.getStatus());
        }

        // Atualizar status para FINALIZADO
        pedidoGateway.atualizarStatus(id, StatusPedido.FINALIZADO);

        // Publicar evento PedidoRetirado
        eventPublisher.publicarPedidoRetirado(id);

        // Buscar pedido atualizado
        return pedidoGateway.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
}
