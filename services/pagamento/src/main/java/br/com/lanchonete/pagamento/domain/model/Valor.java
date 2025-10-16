package br.com.lanchonete.pagamento.domain.model;

import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;

import java.math.BigDecimal;
import java.util.Objects;

public class Valor {

    private final BigDecimal valor;

    public Valor(BigDecimal valor) {
        validar(valor);
        this.valor = valor;
    }

    private void validar(BigDecimal valor) {
        if (valor == null) {
            throw new ValidacaoException("Valor não pode ser nulo");
        }

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("Valor deve ser maior que zero");
        }
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Valor valor1 = (Valor) o;
        return Objects.equals(valor, valor1.valor);
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
