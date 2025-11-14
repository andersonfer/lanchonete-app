package br.com.lanchonete.cozinha.domain.exception;

public class PedidoNaoEncontradoException extends RuntimeException {
    public PedidoNaoEncontradoException(String message) {
        super(message);
    }

    public PedidoNaoEncontradoException(Long id) {
        super("Pedido não encontrado na fila da cozinha com ID: " + id);
    }
}
