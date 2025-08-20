package br.com.lanchonete.auth.domain.exceptions;

public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String message) {
        super(message);
    }

}