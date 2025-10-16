package br.com.lanchonete.clientes.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Cpf {
    private static final Pattern CPF_REGEX = Pattern.compile("^\\d{11}$");

    private final String valor;

    public Cpf(final String cpf) {
        if (Objects.isNull(cpf) || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (!isCpfValido(cpf)) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos numéricos");
        }
        this.valor = cpf;
    }

    private boolean isCpfValido(final String cpf) {
        return CPF_REGEX.matcher(cpf).matches();
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cpf cpf = (Cpf) obj;
        return Objects.equals(valor, cpf.valor);
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
