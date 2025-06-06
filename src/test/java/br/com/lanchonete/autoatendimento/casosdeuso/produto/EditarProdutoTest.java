package br.com.lanchonete.autoatendimento.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EditarProdutoTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private EditarProduto editarProduto;

    private ProdutoRequestDTO produtoValido;
    private Produto produtoExistente;
    private Produto produtoAtualizado;

    @BeforeEach
    void configurar() {
        // Produto existente no repositório
        produtoExistente = Produto.criarSemValidacao(1L, "X-Bacon",
                "Hambúrguer com bacon", new BigDecimal("28.90"), Categoria.LANCHE);

        // Produto com dados atualizados
        produtoValido = new ProdutoRequestDTO("X-Bacon Especial",
                "Hambúrguer com bacon crocante e molho especial",new BigDecimal("32.90"), Categoria.LANCHE);

        // Produto após atualização
        produtoAtualizado = Produto.criarSemValidacao(1L, "X-Bacon Especial",
                "Hambúrguer com bacon crocante e molho especial", new BigDecimal("32.90"), Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve editar produto com sucesso quando os dados são válidos")
    void t1() {

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepositorio.atualizar(any(Produto.class))).thenReturn(produtoAtualizado);


        ProdutoResponseDTO response = editarProduto.executar(1L, produtoValido);

        assertNotNull(response, "A resposta não deveria ser nula");
        assertEquals(1L, response.id(), "O ID do produto atualizado deveria ser 1");
        assertEquals("X-Bacon Especial", response.nome(), "O nome do produto atualizado está incorreto");
        assertEquals(new BigDecimal("32.90"), response.preco(), "O preço do produto atualizado está incorreto");
        assertEquals(Categoria.LANCHE, response.categoria(), "A categoria do produto atualizado está incorreta");

        verify(produtoRepositorio).buscarPorId(1L);
        verify(produtoRepositorio).atualizar(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o ID é nulo")
    void t2() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(null, produtoValido),
                "Deveria lançar uma exceção para ID nulo");

        assertEquals("ID do produto é obrigatório", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio, never()).buscarPorId(any());
        verify(produtoRepositorio, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void t3() {

        when(produtoRepositorio.buscarPorId(999L)).thenReturn(Optional.empty());


        assertThrows(RecursoNaoEncontradoException.class,
                () -> editarProduto.executar(999L, produtoValido),
                "Deveria lançar uma exceção quando o produto não existe");

        verify(produtoRepositorio).buscarPorId(999L);
        verify(produtoRepositorio, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já existe para outro produto")
    void t4() {

        Produto outroProduto = Produto.criarSemValidacao(2L, "X-Salada",
                "Hambúrguer com salada", new BigDecimal("26.90"), Categoria.LANCHE);

        when(produtoRepositorio.buscarPorId(2L)).thenReturn(Optional.of(outroProduto));

        // Nome que já existe para o produto com ID 1
        ProdutoRequestDTO novoProdutoComNomeDuplicado = new ProdutoRequestDTO("X-Bacon",
                "Hambúrguer com salada especial", new BigDecimal("27.90"), Categoria.LANCHE);


        when(produtoRepositorio.existePorNome("X-Bacon")).thenReturn(true);


        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(2L, novoProdutoComNomeDuplicado),
                "Deveria lançar uma exceção quando o nome já existe para outro produto");

        assertEquals("Já existe um produto com este nome", exception.getMessage(),
                "Mensagem da exceção está incorreta");

        verify(produtoRepositorio).buscarPorId(2L);
        verify(produtoRepositorio).existePorNome("X-Bacon");
        verify(produtoRepositorio, never()).atualizar(any());
    }

    @Test
    @DisplayName("Deve permitir editar mantendo o mesmo nome para o produto")
    void t5() {

        // Mesmo nome
        ProdutoRequestDTO produtoParaEditarMatendoNome = new ProdutoRequestDTO("X-Bacon",
                "Hambúrguer com bacon e queijo", new BigDecimal("29.90"), Categoria.LANCHE);

        Produto produtoAtualizadoMesmoNome = Produto.criarSemValidacao(1L, "X-Bacon",
                "Hambúrguer com bacon e queijo", new BigDecimal("29.90"), Categoria.LANCHE);

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepositorio.atualizar(any(Produto.class))).thenReturn(produtoAtualizadoMesmoNome);


        ProdutoResponseDTO response = editarProduto.executar(1L, produtoParaEditarMatendoNome);


        assertNotNull(response);
        assertEquals("X-Bacon", response.nome());
        assertEquals(new BigDecimal("29.90"), response.preco());

        verify(produtoRepositorio).buscarPorId(1L);
        verify(produtoRepositorio).atualizar(any(Produto.class));
        verify(produtoRepositorio, never()).existePorNome("X-Bacon");
    }

    @Test
    @DisplayName("Deve lançar exceção para campos inválidos")
    void t6() {

        when(produtoRepositorio.buscarPorId(1L)).thenReturn(Optional.of(produtoExistente));


        ProdutoRequestDTO produtoParaEditarNomeVazio = new ProdutoRequestDTO("",
                "Descrição teste",new BigDecimal("29.90"), Categoria.LANCHE);


        ValidacaoException exceptionNome = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, produtoParaEditarNomeVazio),
                "Deveria lançar uma exceção para nome vazio");

        assertEquals("Nome do produto é obrigatório", exceptionNome.getMessage());


        ProdutoRequestDTO produtoParaEditarPrecoNulo = new ProdutoRequestDTO("X-Bacon Especial",
                "Descrição teste", null, Categoria.LANCHE);


        ValidacaoException exceptionPreco = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, produtoParaEditarPrecoNulo),
                "Deveria lançar uma exceção para preço nulo");

        assertEquals("Preço do produto é obrigatório", exceptionPreco.getMessage());


        ProdutoRequestDTO produtoParaEditarPrecoZero = new ProdutoRequestDTO("X-Bacon Especial",
                "Descrição teste", BigDecimal.ZERO, Categoria.LANCHE);


        ValidacaoException exceptionPrecoZero = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, produtoParaEditarPrecoZero),
                "Deveria lançar uma exceção para preço zero");

        assertEquals("Preço deve ser maior que zero", exceptionPrecoZero.getMessage());


        ProdutoRequestDTO produtoParaEditarCategoriaNula = new ProdutoRequestDTO("X-Bacon Especial","Descrição teste",new BigDecimal("29.90"),null);


        ValidacaoException exceptionCategoria = assertThrows(ValidacaoException.class,
                () -> editarProduto.executar(1L, produtoParaEditarCategoriaNula),
                "Deveria lançar uma exceção para categoria nula");

        assertEquals("Categoria do produto é obrigatória", exceptionCategoria.getMessage());


        verify(produtoRepositorio, never()).atualizar(any());
    }
}