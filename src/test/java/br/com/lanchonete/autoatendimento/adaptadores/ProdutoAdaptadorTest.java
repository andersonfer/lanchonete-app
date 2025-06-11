package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoAdaptadorTest {

    @Mock
    private BuscarProdutosPorCategoria buscarProdutosPorCategoria;

    @Mock
    private CriarProduto criarProduto;

    @Mock
    private EditarProduto editarProduto;

    @Mock
    private RemoverProduto removerProduto;

    @InjectMocks
    private ProdutoAdaptador produtoAdaptador;

    private ProdutoRequestDTO produtoRequest;
    private ProdutoResponseDTO produtoResponse;

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
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria com sucesso quando use case executa corretamente")
    void t1() {
        List<ProdutoResponseDTO> produtos = Arrays.asList(produtoResponse);
        when(buscarProdutosPorCategoria.executar(Categoria.LANCHE)).thenReturn(produtos);

        List<ProdutoResponseDTO> resultado = produtoAdaptador.buscarPorCategoria(Categoria.LANCHE);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(produtoResponse, resultado.get(0));
        verify(buscarProdutosPorCategoria).executar(Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando use case retorna lista vazia")
    void t2() {
        when(buscarProdutosPorCategoria.executar(Categoria.LANCHE)).thenReturn(Collections.emptyList());

        List<ProdutoResponseDTO> resultado = produtoAdaptador.buscarPorCategoria(Categoria.LANCHE);

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
            produtoAdaptador.buscarPorCategoria(null);
        });
    }

    @Test
    @DisplayName("Deve criar produto com sucesso quando use case executa corretamente")
    void t4() {
        when(criarProduto.executar(any(ProdutoRequestDTO.class))).thenReturn(produtoResponse);

        ProdutoResponseDTO resultado = produtoAdaptador.criar(produtoRequest);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("X-Bacon", resultado.nome());
        assertEquals("Hambúrguer com bacon", resultado.descricao());
        assertEquals(new BigDecimal("25.90"), resultado.preco());
        assertEquals(Categoria.LANCHE, resultado.categoria());
        verify(criarProduto).executar(any(ProdutoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve propagar exceção quando criar produto lança ValidacaoException")
    void t5() {
        when(criarProduto.executar(any(ProdutoRequestDTO.class)))
                .thenThrow(new ValidacaoException("Já existe um produto com este nome"));

        assertThrows(ValidacaoException.class, () -> {
            produtoAdaptador.criar(produtoRequest);
        });
    }

    @Test
    @DisplayName("Deve editar produto com sucesso quando use case executa corretamente")
    void t6() {
        when(editarProduto.executar(eq(1L), any(ProdutoRequestDTO.class))).thenReturn(produtoResponse);

        ProdutoResponseDTO resultado = produtoAdaptador.editar(1L, produtoRequest);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("X-Bacon", resultado.nome());
        assertEquals("Hambúrguer com bacon", resultado.descricao());
        assertEquals(new BigDecimal("25.90"), resultado.preco());
        assertEquals(Categoria.LANCHE, resultado.categoria());
        verify(editarProduto).executar(eq(1L), any(ProdutoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve propagar exceção quando editar produto lança RecursoNaoEncontradoException")
    void t7() {
        when(editarProduto.executar(eq(999L), any(ProdutoRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            produtoAdaptador.editar(999L, produtoRequest);
        });
    }

    @Test
    @DisplayName("Deve propagar exceção quando editar produto lança ValidacaoException")
    void t8() {
        when(editarProduto.executar(eq(1L), any(ProdutoRequestDTO.class)))
                .thenThrow(new ValidacaoException("ID do produto é obrigatório"));

        assertThrows(ValidacaoException.class, () -> {
            produtoAdaptador.editar(1L, produtoRequest);
        });
    }

    @Test
    @DisplayName("Deve remover produto com sucesso quando use case executa corretamente")
    void t9() {
        assertDoesNotThrow(() -> {
            produtoAdaptador.remover(1L);
        });

        verify(removerProduto).executar(1L);
    }

    @Test
    @DisplayName("Deve propagar exceção quando remover produto lança RecursoNaoEncontradoException")
    void t10() {
        doThrow(new RecursoNaoEncontradoException("Produto não encontrado"))
                .when(removerProduto).executar(999L);

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            produtoAdaptador.remover(999L);
        });
    }

    @Test
    @DisplayName("Deve propagar exceção quando remover produto lança ValidacaoException")
    void t11() {
        doThrow(new ValidacaoException("ID do produto é obrigatório"))
                .when(removerProduto).executar(null);

        assertThrows(ValidacaoException.class, () -> {
            produtoAdaptador.remover(null);
        });
    }
}