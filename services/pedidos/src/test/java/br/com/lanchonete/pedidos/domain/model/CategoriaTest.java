package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaTest {

    @Test
    @DisplayName("Deve ter valor LANCHE")
    void t1() {
        assertEquals("LANCHE", Categoria.LANCHE.name());
    }

    @Test
    @DisplayName("Deve ter valor ACOMPANHAMENTO")
    void t2() {
        assertEquals("ACOMPANHAMENTO", Categoria.ACOMPANHAMENTO.name());
    }

    @Test
    @DisplayName("Deve ter valor BEBIDA")
    void t3() {
        assertEquals("BEBIDA", Categoria.BEBIDA.name());
    }

    @Test
    @DisplayName("Deve ter valor SOBREMESA")
    void t4() {
        assertEquals("SOBREMESA", Categoria.SOBREMESA.name());
    }

    @Test
    @DisplayName("Deve converter string para enum")
    void t5() {
        Categoria categoria = Categoria.valueOf("LANCHE");
        assertEquals(Categoria.LANCHE, categoria);
    }

    @Test
    @DisplayName("Deve retornar todos os valores do enum")
    void t6() {
        Categoria[] valores = Categoria.values();
        assertEquals(4, valores.length);
    }
}
