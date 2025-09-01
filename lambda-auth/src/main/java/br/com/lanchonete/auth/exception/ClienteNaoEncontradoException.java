package br.com.lanchonete.auth.exception;

public class ClienteNaoEncontradoException extends Exception {

    public ClienteNaoEncontradoException(String cpf) {
        super("Cliente não encontrado para CPF: " + cpf);
    }

    public ClienteNaoEncontradoException(String cpf, Throwable cause) {
        super("Cliente não encontrado para CPF: " + cpf, cause);
    }
}