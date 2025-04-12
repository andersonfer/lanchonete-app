package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.saida;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.RegistroNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class ProdutoRepositorioJDBCTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProdutoRepositorioJDBC produtoRepositorio;

    private Produto produtoPreCadastrado;

    @BeforeEach
    void configurar() {
        produtoRepositorio = new ProdutoRepositorioJDBC(jdbcTemplate);

        produtoPreCadastrado = Produto.builder()
                .nome("Hambúrguer Clássico")
                .descricao("Hambúrguer com queijo, alface e tomate")
                .preco(new BigDecimal("25.90"))
                .categoria(Categoria.LANCHE)
                .build();

        produtoPreCadastrado = produtoRepositorio.salvar(produtoPreCadastrado);
    }

    @Test
    @DisplayName("Deve salvar um produto com sucesso")
    void t1() {
        Produto novoProduto = Produto.builder()
                .nome("Batata Frita")
                .descricao("Porção de batata frita crocante")
                .preco(new BigDecimal("15.90"))
                .categoria(Categoria.ACOMPANHAMENTO)
                .build();

        Produto produtoSalvo = produtoRepositorio.salvar(novoProduto);

        assertNotNull(produtoSalvo.getId(), "O ID do produto deve ser gerado");
        assertEquals("Batata Frita", produtoSalvo.getNome());
        assertEquals("Porção de batata frita crocante", produtoSalvo.getDescricao());
        assertEquals(new BigDecimal("15.90"), produtoSalvo.getPreco());
        assertEquals(Categoria.ACOMPANHAMENTO, produtoSalvo.getCategoria());
    }

    @Test
    @DisplayName("Deve atualizar um produto com sucesso")
    void t2() {
        produtoPreCadastrado.setPreco(new BigDecimal("29.90"));

        Produto produtoAtualizado = produtoRepositorio.atualizar(produtoPreCadastrado);

        assertEquals(new BigDecimal("29.90"), produtoAtualizado.getPreco());

        // Verifica se foi realmente atualizado no banco
        Optional<Produto> produtoRecuperado = produtoRepositorio.buscarPorId(produtoPreCadastrado.getId());
        assertTrue(produtoRecuperado.isPresent());
        assertEquals(new BigDecimal("29.90"), produtoRecuperado.get().getPreco());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar produto inexistente")
    void t3() {
        Produto produtoInexistente = Produto.builder()
                .id(999L)
                .nome("Produto que não existe")
                .descricao("Descrição")
                .preco(new BigDecimal("10.00"))
                .categoria(Categoria.LANCHE)
                .build();

        assertThrows(RegistroNaoEncontradoException.class,
                () -> produtoRepositorio.atualizar(produtoInexistente),
                "Deve lançar exceção ao tentar atualizar produto inexistente");
    }

    @Test
    @DisplayName("Deve remover um produto com sucesso")
    void t4() {
        produtoRepositorio.remover(produtoPreCadastrado.getId());

        Optional<Produto> produtoRemovido = produtoRepositorio.buscarPorId(produtoPreCadastrado.getId());
        assertTrue(produtoRemovido.isEmpty(), "O produto deve ser removido");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar remover produto inexistente")
    void t5() {
        assertThrows(RegistroNaoEncontradoException.class,
                () -> produtoRepositorio.remover(999L),
                "Deve lançar exceção ao tentar remover produto inexistente");
    }

    @Test
    @DisplayName("Deve encontrar produto por ID")
    void t6() {
        Optional<Produto> resultado = produtoRepositorio.buscarPorId(produtoPreCadastrado.getId());

        assertTrue(resultado.isPresent(), "O produto deve ser encontrado");
        assertEquals(produtoPreCadastrado.getNome(), resultado.get().getNome());
        assertEquals(produtoPreCadastrado.getCategoria(), resultado.get().getCategoria());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar ID inexistente")
    void t7() {
        Optional<Produto> resultado = produtoRepositorio.buscarPorId(999L);
        assertTrue(resultado.isEmpty(), "Não deve encontrar produto com ID inexistente");
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void t8() {
        // Adiciona outro produto na mesma categoria
        Produto outroProduto = Produto.builder()
                .nome("X-Bacon")
                .descricao("Hambúrguer com queijo e bacon")
                .preco(new BigDecimal("28.90"))
                .categoria(Categoria.LANCHE)
                .build();
        produtoRepositorio.salvar(outroProduto);

        // Adiciona produto de outra categoria
        Produto produtoOutraCategoria = Produto.builder()
                .nome("Refrigerante")
                .descricao("Cola 350ml")
                .preco(new BigDecimal("6.90"))
                .categoria(Categoria.BEBIDA)
                .build();
        produtoRepositorio.salvar(produtoOutraCategoria);

        List<Produto> produtosLanche = produtoRepositorio.buscarPorCategoria(Categoria.LANCHE);
        List<Produto> produtosBebida = produtoRepositorio.buscarPorCategoria(Categoria.BEBIDA);

        assertEquals(2, produtosLanche.size(), "Deve encontrar 2 produtos na categoria LANCHE");
        assertEquals(1, produtosBebida.size(), "Deve encontrar 1 produto na categoria BEBIDA");
        assertEquals("Refrigerante", produtosBebida.get(0).getNome());
    }

    @Test
    @DisplayName("Deve retornar lista vazia para categoria sem produtos")
    void t9() {
        List<Produto> produtosSobremesa = produtoRepositorio.buscarPorCategoria(Categoria.SOBREMESA);
        assertTrue(produtosSobremesa.isEmpty(), "Deve retornar lista vazia para categoria sem produtos");
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void t10() {
        // Adiciona mais um produto
        Produto outroProduto = Produto.builder()
                .nome("Milkshake")
                .descricao("Milkshake de chocolate")
                .preco(new BigDecimal("12.90"))
                .categoria(Categoria.SOBREMESA)
                .build();
        produtoRepositorio.salvar(outroProduto);

        List<Produto> todosProdutos = produtoRepositorio.listarTodos();

        assertEquals(2, todosProdutos.size(), "Deve listar todos os produtos cadastrados");
    }

    @Test
    @DisplayName("Deve verificar se produto existe por ID")
    void t11() {
        assertTrue(produtoRepositorio.existePorId(produtoPreCadastrado.getId()),
                "Deve retornar true para ID existente");
        assertFalse(produtoRepositorio.existePorId(999L),
                "Deve retornar false para ID inexistente");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar produto com nome duplicado")
    void t12() {
        // Cria um produto com o mesmo nome do produto pré-cadastrado
        final Produto produtoNomeDuplicado = Produto.builder()
                .nome("Hambúrguer Clássico") // Mesmo nome do produtoPreCadastrado
                .descricao("Outra descrição")
                .preco(new BigDecimal("22.90"))
                .categoria(Categoria.LANCHE)
                .build();

        // Deve lançar exceção por causa da restrição de unicidade
        assertThrows(DataIntegrityViolationException.class,
                () -> produtoRepositorio.salvar(produtoNomeDuplicado),
                "Deve lançar exceção ao tentar salvar produto com nome duplicado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar produto para um nome duplicado")
    void t13() {
        // Cria e salva um segundo produto com nome diferente
        Produto segundoProduto = Produto.builder()
                .nome("X-Salada")
                .descricao("Hambúrguer com alface e tomate")
                .preco(new BigDecimal("23.90"))
                .categoria(Categoria.LANCHE)
                .build();

        Produto produtoSalvo = produtoRepositorio.salvar(segundoProduto);

        // Tenta atualizar o segundo produto para ter o mesmo nome do primeiro
        produtoSalvo.setNome("Hambúrguer Clássico"); // Nome duplicado do produtoPreCadastrado

        // Deve lançar exceção por causa da restrição de unicidade
        assertThrows(DataIntegrityViolationException.class,
                () -> produtoRepositorio.atualizar(produtoSalvo),
                "Deve lançar exceção ao tentar atualizar produto para um nome duplicado");
    }

    @Test
    @DisplayName("Deve verificar se produto existe por nome")
    void t14() {
        assertTrue(produtoRepositorio.existePorNome("Hambúrguer Clássico"),
                "Deve retornar true para nome existente");
        assertFalse(produtoRepositorio.existePorNome("Produto Inexistente"),
                "Deve retornar false para nome inexistente");
    }
}