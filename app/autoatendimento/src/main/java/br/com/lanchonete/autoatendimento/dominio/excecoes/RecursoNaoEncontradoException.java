package br.com.lanchonete.autoatendimento.dominio.excecoes;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}