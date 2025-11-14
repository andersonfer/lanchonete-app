package br.com.lanchonete.pedidos.domain.exceptions;

public class PedidoNaoEncontradoException extends RuntimeException {
    public PedidoNaoEncontradoException(Long id) {
        super("Pedido não encontrado: " + id);
    }
}
