package br.com.lanchonete.autoatendimento.dominio.shared.excecao;

public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String message) {
        super(message);
    }

}