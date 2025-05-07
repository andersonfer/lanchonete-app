package br.com.lanchonete.autoatendimento.aplicacao.portas.saida;

import br.com.lanchonete.autoatendimento.dominio.modelo.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.StatusPedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositorio {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    List<Pedido> listarTodos();
    void atualizarStatus(Long pedidoId, StatusPedido novoStatus);
}