package br.com.lanchonete.autoatendimento.adaptadores.util;

import java.util.Objects;

public class Utils {
    private Utils() {}

    public static boolean isNuloOuVazio(final String str) {
        return Objects.isNull(str) || str.isBlank();
    }
}
