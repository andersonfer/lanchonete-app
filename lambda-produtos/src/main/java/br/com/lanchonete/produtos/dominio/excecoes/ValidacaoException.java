package br.com.lanchonete.produtos.dominio.excecoes;

public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String message) {
        super(message);
    }

}