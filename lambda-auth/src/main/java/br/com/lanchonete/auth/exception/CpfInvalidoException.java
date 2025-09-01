package br.com.lanchonete.auth.exception;

public class CpfInvalidoException extends Exception {

    public CpfInvalidoException(String cpf) {
        super("CPF inválido: " + cpf);
    }

    public CpfInvalidoException(String cpf, Throwable cause) {
        super("CPF inválido: " + cpf, cause);
    }
}