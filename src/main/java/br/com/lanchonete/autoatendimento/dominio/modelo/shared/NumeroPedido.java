package br.com.lanchonete.autoatendimento.dominio.modelo.shared;

import java.util.Objects;

public final class NumeroPedido {
    
    private final String valor;

    private NumeroPedido(final String numero) {
        if (Objects.isNull(numero) || numero.isBlank()) {
            throw new IllegalArgumentException("Número do pedido é obrigatório");
        }
        if (!isNumeroValido(numero)) {
            throw new IllegalArgumentException("Número do pedido deve conter apenas letras e números");
        }
        this.valor = numero.toUpperCase();
    }

    public static NumeroPedido gerarPorId(final Long id) {
        if (Objects.isNull(id) || id <= 0) {
            throw new IllegalArgumentException("ID deve ser um valor positivo");
        }
        return new NumeroPedido(String.format("PED%06d", id));
    }

    private boolean isNumeroValido(final String numero) {
        return numero.matches("^[A-Za-z0-9]+$");
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumeroPedido numeroPedido = (NumeroPedido) obj;
        return Objects.equals(valor, numeroPedido.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}