package br.com.lanchonete.clientes.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String valor;

    public Email(final String email) {
        if (Objects.isNull(email) || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!isEmailValido(email)) {
            throw new IllegalArgumentException("Email inválido");
        }
        this.valor = email;
    }

    private boolean isEmailValido(final String email) {
        return EMAIL_REGEX.matcher(email).matches();
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email emailObj = (Email) obj;
        return Objects.equals(valor, emailObj.valor);
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
