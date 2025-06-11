package br.com.lanchonete.autoatendimento.dominio.shared.excecao;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}