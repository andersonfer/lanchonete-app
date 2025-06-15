package br.com.lanchonete.autoatendimento.aplicacao.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private BuscarProdutosPorCategoria buscarProdutosPorCategoria;

    @Mock
    private CriarProduto criarProduto;

    @Mock
    private EditarProduto editarProduto;

    @Mock
    private RemoverProduto removerProduto;

    @InjectMocks
    private ProdutoService produtoService;

    private ProdutoRequestDTO produtoRequest;
    private ProdutoResponseDTO produtoResponse;
    private Produto produto;

    @BeforeEach
    void configurar() {
        produtoRequest = new ProdutoRequestDTO(
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("25.90"),
                Categoria.LANCHE
        );

        produtoResponse = new ProdutoResponseDTO(
                1L,
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("25.90"),
                Categoria.LANCHE
        );

        produto = Produto.reconstituir(
                1L,
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("25.90"),
                Categoria.LANCHE
        );
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria com sucesso quando use case executa corretamente")
    void t1() {
        List<Produto> produtos = Arrays.asList(produto);
        when(buscarProdutosPorCategoria.executar(Categoria.LANCHE)).thenReturn(produtos);

        List<ProdutoResponseDTO> resultado = produtoService.buscarPorCategoria(Categoria.LANCHE);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        ProdutoResponseDTO dto = resultado.get(0);
        assertEquals(produto.getId(), dto.id());
        assertEquals(produto.getNome(), dto.nome());
        assertEquals(produto.getDescricao(), dto.descricao());
        assertEquals(produto.getPreco().getValor(), dto.preco());
        assertEquals(produto.getCategoria(), dto.categoria());
        verify(buscarProdutosPorCategoria).executar(Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando use case retorna lista vazia")
    void t2() {
        when(buscarProdutosPorCategoria.executar(Categoria.LANCHE)).thenReturn(Collections.emptyList());

        List<ProdutoResponseDTO> resultado = produtoService.buscarPorCategoria(Categoria.LANCHE);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(buscarProdutosPorCategoria).executar(Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve propagar exceção quando buscar por categoria lança ValidacaoException")
    void t3() {
        when(buscarProdutosPorCategoria.executar(null))
                .thenThrow(new ValidacaoException("Categoria é obrigatória"));

        assertThrows(ValidacaoException.class, () -> {
            produtoService.buscarPorCategoria(null);
        });
    }

    @Test
    @DisplayName("Deve criar produto com sucesso quando use case executa corretamente")
    void t4() {
        when(criarProduto.executar(anyString(), anyString(), any(BigDecimal.class), any(Categoria.class))).thenReturn(produto);

        ProdutoResponseDTO resultado = produtoService.criar(produtoRequest);

        assertNotNull(resultado);
        assertEquals(produto.getId(), resultado.id());
        assertEquals(produto.getNome(), resultado.nome());
        assertEquals(produto.getDescricao(), resultado.descricao());
        assertEquals(produto.getPreco().getValor(), resultado.preco());
        assertEquals(produto.getCategoria(), resultado.categoria());
        verify(criarProduto).executar(anyString(), anyString(), any(BigDecimal.class), any(Categoria.class));
    }

    @Test
    @DisplayName("Deve propagar exceção quando criar produto lança ValidacaoException")
    void t5() {
        when(criarProduto.executar(anyString(), anyString(), any(BigDecimal.class), any(Categoria.class)))
                .thenThrow(new ValidacaoException("Já existe um produto com este nome"));

        assertThrows(ValidacaoException.class, () -> {
            produtoService.criar(produtoRequest);
        });
    }

    @Test
    @DisplayName("Deve editar produto com sucesso quando use case executa corretamente")
    void t6() {
        when(editarProduto.executar(eq(1L), anyString(), anyString(), any(BigDecimal.class), any(Categoria.class))).thenReturn(produto);

        ProdutoResponseDTO resultado = produtoService.editar(1L, produtoRequest);

        assertNotNull(resultado);
        assertEquals(produto.getId(), resultado.id());
        assertEquals(produto.getNome(), resultado.nome());
        assertEquals(produto.getDescricao(), resultado.descricao());
        assertEquals(produto.getPreco().getValor(), resultado.preco());
        assertEquals(produto.getCategoria(), resultado.categoria());
        verify(editarProduto).executar(eq(1L), anyString(), anyString(), any(BigDecimal.class), any(Categoria.class));
    }

    @Test
    @DisplayName("Deve propagar exceção quando editar produto lança RecursoNaoEncontradoException")
    void t7() {
        when(editarProduto.executar(eq(999L), anyString(), anyString(), any(BigDecimal.class), any(Categoria.class)))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            produtoService.editar(999L, produtoRequest);
        });
    }

    @Test
    @DisplayName("Deve propagar exceção quando editar produto lança ValidacaoException")
    void t8() {
        when(editarProduto.executar(eq(1L), anyString(), anyString(), any(BigDecimal.class), any(Categoria.class)))
                .thenThrow(new ValidacaoException("ID do produto é obrigatório"));

        assertThrows(ValidacaoException.class, () -> {
            produtoService.editar(1L, produtoRequest);
        });
    }

    @Test
    @DisplayName("Deve remover produto com sucesso quando use case executa corretamente")
    void t9() {
        assertDoesNotThrow(() -> {
            produtoService.remover(1L);
        });

        verify(removerProduto).executar(1L);
    }

    @Test
    @DisplayName("Deve propagar exceção quando remover produto lança RecursoNaoEncontradoException")
    void t10() {
        doThrow(new RecursoNaoEncontradoException("Produto não encontrado"))
                .when(removerProduto).executar(999L);

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            produtoService.remover(999L);
        });
    }

    @Test
    @DisplayName("Deve propagar exceção quando remover produto lança ValidacaoException")
    void t11() {
        doThrow(new ValidacaoException("ID do produto é obrigatório"))
                .when(removerProduto).executar(null);

        assertThrows(ValidacaoException.class, () -> {
            produtoService.remover(null);
        });
    }
}