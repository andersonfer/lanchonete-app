package br.com.lanchonete.autoatendimento.dominio.modelo.pedido;

public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    REJEITADO;

    public boolean isPendente() {
        return this == PENDENTE;
    }

    public boolean isAprovado() {
        return this == APROVADO;
    }

    public boolean isRejeitado() {
        return this == REJEITADO;
    }

    public boolean foiProcessado() {
        return this == APROVADO || this == REJEITADO;
    }
}