package br.com.lanchonete.pedidos.application.gateways;

import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;

import java.util.List;
import java.util.Optional;

public interface PedidoGateway {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    List<Pedido> listarTodos();
    List<Pedido> listarPorStatus(StatusPedido status);
    void atualizarStatus(Long id, StatusPedido status);
}
