package br.com.lanchonete.clientes.domain.exceptions;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente não encontrado com CPF: " + cpf);
    }
}
