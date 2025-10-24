package br.com.lanchonete.cozinha.domain.exception;

public class StatusInvalidoException extends RuntimeException {
    public StatusInvalidoException(String message) {
        super(message);
    }
}
