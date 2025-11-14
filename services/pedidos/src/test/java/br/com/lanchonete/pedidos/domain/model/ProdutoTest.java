package br.com.lanchonete.pedidos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto com dados válidos")
    void t1() {
        Produto produto = Produto.criar("X-Burger", "Hambúrguer com queijo", new BigDecimal("15.00"), Categoria.LANCHE);

        assertNotNull(produto);
        assertEquals("X-Burger", produto.getNome());
        assertEquals("Hambúrguer com queijo", produto.getDescricao());
        assertEquals(new BigDecimal("15.00"), produto.getPreco().getValor());
        assertEquals(Categoria.LANCHE, produto.getCategoria());
    }

    @Test
    @DisplayName("Deve reconstituir produto com ID")
    void t2() {
        Produto produto = Produto.reconstituir(1L, "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), Categoria.BEBIDA);

        assertNotNull(produto);
        assertEquals(1L, produto.getId());
        assertEquals("Coca-Cola", produto.getNome());
        assertEquals("Refrigerante", produto.getDescricao());
        assertEquals(new BigDecimal("5.00"), produto.getPreco().getValor());
        assertEquals(Categoria.BEBIDA, produto.getCategoria());
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é nulo")
    void t3() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Produto.criar(null, "Descrição", new BigDecimal("10.00"), Categoria.LANCHE);
        });

        assertEquals("Nome do produto é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é vazio")
    void t4() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Produto.criar("", "Descrição", new BigDecimal("10.00"), Categoria.LANCHE);
        });

        assertEquals("Nome do produto é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é apenas espaços")
    void t5() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Produto.criar("   ", "Descrição", new BigDecimal("10.00"), Categoria.LANCHE);
        });

        assertEquals("Nome do produto é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é nulo")
    void t6() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Produto.criar("Produto", "Descrição", null, Categoria.LANCHE);
        });

        assertEquals("Preço é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria é nula")
    void t7() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Produto.criar("Produto", "Descrição", new BigDecimal("10.00"), null);
        });

        assertEquals("Categoria do produto é obrigatória", exception.getMessage());
    }

    @Test
    @DisplayName("Deve permitir descrição nula")
    void t8() {
        Produto produto = Produto.criar("Produto", null, new BigDecimal("10.00"), Categoria.LANCHE);

        assertNotNull(produto);
        assertNull(produto.getDescricao());
    }

    @Test
    @DisplayName("Deve alterar nome do produto")
    void t9() {
        Produto produto = Produto.reconstituir(1L, "Nome Original", "Descrição", new BigDecimal("10.00"), Categoria.LANCHE);

        produto.setNome("Nome Alterado");

        assertEquals("Nome Alterado", produto.getNome());
    }

    @Test
    @DisplayName("Deve alterar preço do produto")
    void t10() {
        Produto produto = Produto.reconstituir(1L, "Produto", "Descrição", new BigDecimal("10.00"), Categoria.LANCHE);

        produto.setPreco(new Preco(new BigDecimal("20.00")));

        assertEquals(new BigDecimal("20.00"), produto.getPreco().getValor());
    }

    @Test
    @DisplayName("Deve alterar categoria do produto")
    void t11() {
        Produto produto = Produto.reconstituir(1L, "Produto", "Descrição", new BigDecimal("10.00"), Categoria.LANCHE);

        produto.setCategoria(Categoria.BEBIDA);

        assertEquals(Categoria.BEBIDA, produto.getCategoria());
    }

    @Test
    @DisplayName("Deve considerar dois produtos iguais quando possuem mesmo ID e dados")
    void t12() {
        Produto produto1 = Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE);
        Produto produto2 = Produto.reconstituir(1L, "X-Burger", "Hambúrguer", new BigDecimal("15.00"), Categoria.LANCHE);

        assertEquals(produto1, produto2);
        assertEquals(produto1.hashCode(), produto2.hashCode());
    }

    @Test
    @DisplayName("Deve alterar descrição do produto")
    void t13() {
        Produto produto = Produto.reconstituir(1L, "Produto", "Descrição Original", new BigDecimal("10.00"), Categoria.LANCHE);

        produto.setDescricao("Nova Descrição");

        assertEquals("Nova Descrição", produto.getDescricao());
    }
}
