package br.com.lanchonete.autoatendimento.aplicacao.excecao;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
