package br.com.lanchonete.autoatendimento.adaptadores.shared.excecao;

public class RegistroNaoEncontradoException extends RuntimeException {

    public RegistroNaoEncontradoException(String message) {
        super(message);
    }

    public RegistroNaoEncontradoException(String entidade, Long id) {
        super(String.format("%s não encontrado com ID: %d", entidade, id));
    }
}