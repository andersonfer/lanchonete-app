package br.com.lanchonete.pedidos.domain.exceptions;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente não encontrado: " + cpf);
    }
}
