package br.com.lanchonete.pagamento.domain.exceptions;

public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
