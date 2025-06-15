package br.com.lanchonete.autoatendimento.adaptadores.persistencia;

import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import br.com.lanchonete.autoatendimento.dominio.modelo.shared.Preco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class ProdutoGatewayJDBCTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProdutoGatewayJDBC produtoRepositorio;

    private Produto produtoPreCadastrado;

    @BeforeEach
    void configurar() {
        produtoRepositorio = new ProdutoGatewayJDBC(jdbcTemplate);

        produtoPreCadastrado = Produto.criar(
                "Hambúrguer Clássico",
                "Hambúrguer com queijo, alface e tomate",
                new BigDecimal("25.90"),
                Categoria.LANCHE);

        produtoPreCadastrado = produtoRepositorio.salvar(produtoPreCadastrado);
    }

    @Test
    @DisplayName("Deve salvar um produto com sucesso")
    void t1() {
        Produto novoProduto = Produto.criar(
                "Batata Frita",
                "Porção de batata frita crocante",
                new BigDecimal("15.90"),
                Categoria.ACOMPANHAMENTO);

        Produto produtoSalvo = produtoRepositorio.salvar(novoProduto);

        assertNotNull(produtoSalvo.getId(), "O ID do produto deve ser gerado");
        assertEquals("Batata Frita", produtoSalvo.getNome());
        assertEquals("Porção de batata frita crocante", produtoSalvo.getDescricao());
        assertEquals(new BigDecimal("15.90"), produtoSalvo.getPreco().getValor());
        assertEquals(Categoria.ACOMPANHAMENTO, produtoSalvo.getCategoria());
    }

    @Test
    @DisplayName("Deve atualizar um produto com sucesso")
    void t2() {
        produtoPreCadastrado.setPreco(new Preco(new BigDecimal("29.90")));

        Produto produtoAtualizado = produtoRepositorio.atualizar(produtoPreCadastrado);

        assertEquals(new BigDecimal("29.90"), produtoAtualizado.getPreco().getValor());

        // Verifica se foi realmente atualizado no banco
        Optional<Produto> produtoRecuperado = produtoRepositorio.buscarPorId(produtoPreCadastrado.getId());
        assertTrue(produtoRecuperado.isPresent());
        assertEquals(new BigDecimal("29.90"), produtoRecuperado.get().getPreco().getValor());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar produto inexistente")
    void t3() {
        Produto produtoInexistente = Produto.reconstituir(
                999L,
                "Produto que não existe",
                "Descrição",
                new BigDecimal("10.00"),
                Categoria.LANCHE);

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
        Produto outroProduto = Produto.criar(
                "X-Bacon",
                "Hambúrguer com queijo e bacon",
                new BigDecimal("28.90"),
                Categoria.LANCHE);
        produtoRepositorio.salvar(outroProduto);

        // Adiciona produto de outra categoria
        Produto produtoOutraCategoria = Produto.criar(
                "Refrigerante",
                "Cola 350ml",
                new BigDecimal("6.90"),
                Categoria.BEBIDA);
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
        Produto outroProduto = Produto.criar(
                "Milkshake",
                "Milkshake de chocolate",
                new BigDecimal("12.90"),
                Categoria.SOBREMESA);
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
        final Produto produtoNomeDuplicado = Produto.criar(
                "Hambúrguer Clássico", // Mesmo nome do produtoPreCadastrado
                "Outra descrição",
                new BigDecimal("22.90"),
                Categoria.LANCHE);

        // Deve lançar exceção por causa da restrição de unicidade
        assertThrows(DataIntegrityViolationException.class,
                () -> produtoRepositorio.salvar(produtoNomeDuplicado),
                "Deve lançar exceção ao tentar salvar produto com nome duplicado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar produto para um nome duplicado")
    void t13() {
        // Cria e salva um segundo produto com nome diferente
        Produto segundoProduto = Produto.criar(
                "X-Salada",
                "Hambúrguer com alface e tomate",
                new BigDecimal("23.90"),
                Categoria.LANCHE);

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