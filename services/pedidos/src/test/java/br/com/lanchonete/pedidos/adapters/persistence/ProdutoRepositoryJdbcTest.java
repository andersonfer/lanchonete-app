package br.com.lanchonete.pedidos.adapters.persistence;

import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class ProdutoRepositoryJdbcTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProdutoRepositoryJdbc produtoRepository;

    @BeforeEach
    void configurar() {
        produtoRepository = new ProdutoRepositoryJdbc(jdbcTemplate);

        // Inserir produtos de teste
        jdbcTemplate.update(
                "INSERT INTO produtos (nome, descricao, preco, categoria) VALUES (?, ?, ?, ?)",
                "X-Burger", "Hambúrguer com queijo", new BigDecimal("15.00"), "LANCHE");

        jdbcTemplate.update(
                "INSERT INTO produtos (nome, descricao, preco, categoria) VALUES (?, ?, ?, ?)",
                "X-Salada", "Hambúrguer com salada", new BigDecimal("18.00"), "LANCHE");

        jdbcTemplate.update(
                "INSERT INTO produtos (nome, descricao, preco, categoria) VALUES (?, ?, ?, ?)",
                "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), "BEBIDA");
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void t1() {
        Optional<Produto> resultado = produtoRepository.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        Produto produto = resultado.get();
        assertEquals(1L, produto.getId());
        assertEquals("X-Burger", produto.getNome());
        assertEquals(new BigDecimal("15.00"), produto.getPreco().getValor());
        assertEquals(Categoria.LANCHE, produto.getCategoria());
    }

    @Test
    @DisplayName("Deve retornar vazio quando produto não existe")
    void t2() {
        Optional<Produto> resultado = produtoRepository.buscarPorId(999L);

        assertFalse(resultado.isPresent());
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void t3() {
        List<Produto> produtos = produtoRepository.listarTodos();

        assertNotNull(produtos);
        assertEquals(3, produtos.size());
    }

    @Test
    @DisplayName("Deve listar produtos por categoria LANCHE")
    void t4() {
        List<Produto> lanches = produtoRepository.listarPorCategoria(Categoria.LANCHE);

        assertNotNull(lanches);
        assertEquals(2, lanches.size());
        assertTrue(lanches.stream().allMatch(p -> p.getCategoria() == Categoria.LANCHE));
    }

    @Test
    @DisplayName("Deve listar produtos por categoria BEBIDA")
    void t5() {
        List<Produto> bebidas = produtoRepository.listarPorCategoria(Categoria.BEBIDA);

        assertNotNull(bebidas);
        assertEquals(1, bebidas.size());
        assertEquals(Categoria.BEBIDA, bebidas.get(0).getCategoria());
        assertEquals("Coca-Cola", bebidas.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia para categoria sem produtos")
    void t6() {
        List<Produto> sobremesas = produtoRepository.listarPorCategoria(Categoria.SOBREMESA);

        assertNotNull(sobremesas);
        assertTrue(sobremesas.isEmpty());
    }
}
