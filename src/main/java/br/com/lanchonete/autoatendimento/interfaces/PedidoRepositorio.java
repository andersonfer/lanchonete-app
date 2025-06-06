package br.com.lanchonete.autoatendimento.interfaces;

import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.StatusPedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositorio {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    List<Pedido> listarTodos();
    void atualizarStatus(Long pedidoId, StatusPedido novoStatus);
}