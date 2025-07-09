package br.com.lanchonete.autoatendimento.dominio.excecoes;

public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String message) {
        super(message);
    }

}