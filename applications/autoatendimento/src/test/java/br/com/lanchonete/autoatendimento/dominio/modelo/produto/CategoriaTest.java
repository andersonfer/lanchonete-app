package br.com.lanchonete.autoatendimento.dominio.modelo.produto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaTest {

    @Test
    @DisplayName("Deve conter todas as categorias esperadas")
    void t1() {
        // Verificar se todos os valores esperados existem
        Categoria[] categorias = Categoria.values();

        assertEquals(4, categorias.length, "Deve conter exatamente 4 categorias");
        assertTrue(containsCategory(categorias, Categoria.LANCHE), "Deve conter categoria LANCHE");
        assertTrue(containsCategory(categorias, Categoria.ACOMPANHAMENTO), "Deve conter categoria ACOMPANHAMENTO");
        assertTrue(containsCategory(categorias, Categoria.BEBIDA), "Deve conter categoria BEBIDA");
        assertTrue(containsCategory(categorias, Categoria.SOBREMESA), "Deve conter categoria SOBREMESA");
    }

    @Test
    @DisplayName("Deve converter string para categoria - LANCHE")
    void t2() {
        Categoria categoria = Categoria.valueOf("LANCHE");
        assertEquals(Categoria.LANCHE, categoria, "Deve converter LANCHE corretamente");
    }

    @Test
    @DisplayName("Deve converter string para categoria - ACOMPANHAMENTO")
    void t3() {
        Categoria categoria = Categoria.valueOf("ACOMPANHAMENTO");
        assertEquals(Categoria.ACOMPANHAMENTO, categoria, "Deve converter ACOMPANHAMENTO corretamente");
    }

    @Test
    @DisplayName("Deve converter string para categoria - BEBIDA")
    void t4() {
        Categoria categoria = Categoria.valueOf("BEBIDA");
        assertEquals(Categoria.BEBIDA, categoria, "Deve converter BEBIDA corretamente");
    }

    @Test
    @DisplayName("Deve converter string para categoria - SOBREMESA")
    void t5() {
        Categoria categoria = Categoria.valueOf("SOBREMESA");
        assertEquals(Categoria.SOBREMESA, categoria, "Deve converter SOBREMESA corretamente");
    }

    @Test
    @DisplayName("Deve lançar exceção para categoria inválida")
    void t6() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Categoria.valueOf("CATEGORIA_INEXISTENTE"),
                "Deve lançar exceção para categoria inválida"
        );

        assertNotNull(exception.getMessage(), "Mensagem de erro deve estar presente");
    }

    @Test
    @DisplayName("Deve ter nomes corretos para cada categoria")
    void t7() {
        assertEquals("LANCHE", Categoria.LANCHE.name(), "Nome da categoria LANCHE deve estar correto");
        assertEquals("ACOMPANHAMENTO", Categoria.ACOMPANHAMENTO.name(), "Nome da categoria ACOMPANHAMENTO deve estar correto");
        assertEquals("BEBIDA", Categoria.BEBIDA.name(), "Nome da categoria BEBIDA deve estar correto");
        assertEquals("SOBREMESA", Categoria.SOBREMESA.name(), "Nome da categoria SOBREMESA deve estar correto");
    }

    @Test
    @DisplayName("Deve ser igual apenas a si mesma")
    void t8() {
        assertEquals(Categoria.LANCHE, Categoria.LANCHE, "Categoria deve ser igual a si mesma");
        assertNotEquals(Categoria.LANCHE, Categoria.BEBIDA, "Categorias diferentes devem ser diferentes");
        assertNotEquals(Categoria.ACOMPANHAMENTO, Categoria.SOBREMESA, "Categorias diferentes devem ser diferentes");
    }

    @Test
    @DisplayName("Deve ter toString igual ao name")
    void t9() {
        assertEquals("LANCHE", Categoria.LANCHE.toString(), "ToString de LANCHE deve ser igual ao name");
        assertEquals("ACOMPANHAMENTO", Categoria.ACOMPANHAMENTO.toString(), "ToString de ACOMPANHAMENTO deve ser igual ao name");
        assertEquals("BEBIDA", Categoria.BEBIDA.toString(), "ToString de BEBIDA deve ser igual ao name");
        assertEquals("SOBREMESA", Categoria.SOBREMESA.toString(), "ToString de SOBREMESA deve ser igual ao name");
    }

    @Test
    @DisplayName("Deve funcionar com switch statement")
    void t10() {
        String resultado = switch (Categoria.LANCHE) {
            case LANCHE -> "É um lanche";
            case ACOMPANHAMENTO -> "É um acompanhamento";
            case BEBIDA -> "É uma bebida";
            case SOBREMESA -> "É uma sobremesa";
        };

        assertEquals("É um lanche", resultado, "Switch deve funcionar corretamente");
    }

    @Test
    @DisplayName("Deve ser utilizável em comparações de igualdade")
    void t11() {
        Categoria categoria1 = Categoria.BEBIDA;
        Categoria categoria2 = Categoria.valueOf("BEBIDA");

        assertTrue(categoria1 == categoria2, "Instâncias da mesma categoria devem ser iguais com ==");
        assertEquals(categoria1, categoria2, "Instâncias da mesma categoria devem ser iguais com equals");
    }

    private boolean containsCategory(Categoria[] categorias, Categoria categoria) {
        for (Categoria cat : categorias) {
            if (cat == categoria) {
                return true;
            }
        }
        return false;
    }
}