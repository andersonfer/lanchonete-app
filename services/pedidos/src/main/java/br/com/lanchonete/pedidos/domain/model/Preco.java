package br.com.lanchonete.pedidos.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Preco {

    private final BigDecimal valor;

    public Preco(final BigDecimal preco) {
        if (Objects.isNull(preco)) {
            throw new IllegalArgumentException("Preço é obrigatório");
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }
        this.valor = preco;
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Preco preco = (Preco) obj;
        return Objects.equals(valor, preco.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
