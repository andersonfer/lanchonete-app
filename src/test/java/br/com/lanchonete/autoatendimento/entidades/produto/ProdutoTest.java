package br.com.lanchonete.autoatendimento.entidades.produto;

import br.com.lanchonete.autoatendimento.entidades.shared.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto com sucesso quando os dados são válidos")
    void t1() {
        // Criar produto com dados válidos
        Produto produto = Produto.criar("X-Bacon", "Hambúrguer com bacon", new BigDecimal("25.90"), Categoria.LANCHE);

        // Verificações
        assertEquals("X-Bacon", produto.getNome(), "Nome deve estar correto");
        assertEquals("Hambúrguer com bacon", produto.getDescricao(), "Descrição deve estar correta");
        assertEquals(new BigDecimal("25.90"), produto.getPreco().getValor(), "Preço deve estar correto");
        assertEquals(Categoria.LANCHE, produto.getCategoria(), "Categoria deve estar correta");
        assertNull(produto.getId(), "ID deve ser nulo para produto novo");
    }

    @Test
    @DisplayName("Deve criar produto com descrição nula")
    void t2() {
        // Criar produto com descrição nula (permitido)
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Verificações
        assertEquals("X-Bacon", produto.getNome(), "Nome deve estar correto");
        assertNull(produto.getDescricao(), "Descrição pode ser nula");
        assertEquals(new BigDecimal("25.90"), produto.getPreco().getValor(), "Preço deve estar correto");
        assertEquals(Categoria.LANCHE, produto.getCategoria(), "Categoria deve estar correta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com nome nulo")
    void t3() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar(null, "Descrição", new BigDecimal("25.90"), Categoria.LANCHE),
                "Deve lançar exceção para nome nulo"
        );

        assertEquals("Nome do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com nome vazio")
    void t4() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar("", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE),
                "Deve lançar exceção para nome vazio"
        );

        assertEquals("Nome do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com nome em branco")
    void t5() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar("   ", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE),
                "Deve lançar exceção para nome em branco"
        );

        assertEquals("Nome do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com preço nulo")
    void t6() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar("X-Bacon", "Descrição", null, Categoria.LANCHE),
                "Deve lançar exceção para preço nulo"
        );

        assertEquals("Preço é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com preço zero")
    void t7() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar("X-Bacon", "Descrição", BigDecimal.ZERO, Categoria.LANCHE),
                "Deve lançar exceção para preço zero"
        );

        assertEquals("Preço deve ser maior que zero", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com preço negativo")
    void t8() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar("X-Bacon", "Descrição", new BigDecimal("-10.00"), Categoria.LANCHE),
                "Deve lançar exceção para preço negativo"
        );

        assertEquals("Preço deve ser maior que zero", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com categoria nula")
    void t9() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), null),
                "Deve lançar exceção para categoria nula"
        );

        assertEquals("Categoria do produto é obrigatória", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve reconstituir produto com ID a partir de dados do banco")
    void t10() {
        // Reconstituir produto a partir de dados do banco
        Produto produto = Produto.reconstituir(1L, "Refrigerante", "Coca-Cola 350ml", new BigDecimal("6.00"), Categoria.BEBIDA);

        // Verificações
        assertEquals(1L, produto.getId(), "ID deve estar correto");
        assertEquals("Refrigerante", produto.getNome(), "Nome deve estar correto");
        assertEquals("Coca-Cola 350ml", produto.getDescricao(), "Descrição deve estar correta");
        assertEquals(new BigDecimal("6.00"), produto.getPreco().getValor(), "Preço deve estar correto");
        assertEquals(Categoria.BEBIDA, produto.getCategoria(), "Categoria deve estar correta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir nome nulo")
    void t11() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produto.setNome(null),
                "Deve lançar exceção ao definir nome nulo"
        );

        assertEquals("Nome do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir preço nulo")
    void t12() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produto.setPreco(null),
                "Deve lançar exceção ao definir preço nulo"
        );

        assertEquals("Preço do produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir categoria nula")
    void t13() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produto.setCategoria(null),
                "Deve lançar exceção ao definir categoria nula"
        );

        assertEquals("Categoria do produto é obrigatória", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve ser igual quando produtos têm os mesmos dados")
    void t14() {
        Produto produto1 = Produto.reconstituir(1L, "X-Bacon", "Hambúrguer com bacon", new BigDecimal("25.90"), Categoria.LANCHE);
        Produto produto2 = Produto.reconstituir(1L, "X-Bacon", "Hambúrguer com bacon", new BigDecimal("25.90"), Categoria.LANCHE);

        assertEquals(produto1, produto2, "Produtos com mesmos dados devem ser iguais");
        assertEquals(produto1.hashCode(), produto2.hashCode(), "HashCode deve ser igual");
    }

    @Test
    @DisplayName("Deve ser diferente quando produtos têm dados diferentes")
    void t15() {
        Produto produto1 = Produto.reconstituir(1L, "X-Bacon", "Hambúrguer com bacon", new BigDecimal("25.90"), Categoria.LANCHE);
        Produto produto2 = Produto.reconstituir(2L, "Refrigerante", "Coca-Cola 350ml", new BigDecimal("6.00"), Categoria.BEBIDA);

        assertNotEquals(produto1, produto2, "Produtos com dados diferentes devem ser diferentes");
    }

    @Test
    @DisplayName("Deve ter toString bem formatado")
    void t16() {
        Produto produto = Produto.reconstituir(1L, "X-Bacon", "Hambúrguer com bacon", new BigDecimal("25.90"), Categoria.LANCHE);

        String toString = produto.toString();

        assertTrue(toString.contains("Produto{"), "ToString deve começar com Produto{");
        assertTrue(toString.contains("id=1"), "ToString deve conter o ID");
        assertTrue(toString.contains("nome='X-Bacon'"), "ToString deve conter o nome");
        assertTrue(toString.contains("descricao='Hambúrguer com bacon'"), "ToString deve conter a descrição");
        assertTrue(toString.contains("preco="), "ToString deve conter o preço");
        assertTrue(toString.contains("categoria=LANCHE"), "ToString deve conter a categoria");
    }

    @Test
    @DisplayName("Deve permitir alterar nome válido")
    void t17() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        produto.setNome("X-Tudo");

        assertEquals("X-Tudo", produto.getNome(), "Nome deve ter sido alterado");
    }

    @Test
    @DisplayName("Deve permitir alterar descrição")
    void t18() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        produto.setDescricao("Nova descrição");

        assertEquals("Nova descrição", produto.getDescricao(), "Descrição deve ter sido alterada");
    }

    @Test
    @DisplayName("Deve permitir alterar preço válido")
    void t19() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        produto.setPreco(new Preco(new BigDecimal("30.00")));

        assertEquals(new BigDecimal("30.00"), produto.getPreco().getValor(), "Preço deve ter sido alterado");
    }

    @Test
    @DisplayName("Deve permitir alterar categoria válida")
    void t20() {
        Produto produto = Produto.criar("X-Bacon", "Descrição", new BigDecimal("25.90"), Categoria.LANCHE);

        produto.setCategoria(Categoria.ACOMPANHAMENTO);

        assertEquals(Categoria.ACOMPANHAMENTO, produto.getCategoria(), "Categoria deve ter sido alterada");
    }
}