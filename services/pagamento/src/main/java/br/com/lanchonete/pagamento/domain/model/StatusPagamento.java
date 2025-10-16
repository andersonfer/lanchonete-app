package br.com.lanchonete.pagamento.domain.model;

public enum StatusPagamento {
    APROVADO,
    REJEITADO;

    public boolean isAprovado() {
        return this == APROVADO;
    }

    public boolean isRejeitado() {
        return this == REJEITADO;
    }
}
