package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BuscarProdutosPorCategoriaServiceTest {

    @Mock
    private ProdutoRepositorio produtoRepositorio;

    @InjectMocks
    private BuscarProdutosPorCategoriaService buscarProdutosPorCategoriaService;

    private List<Produto> produtosLanche;

    @BeforeEach
    void configurar() {
        produtosLanche = Arrays.asList(
                Produto.builder()
                        .id(1L)
                        .nome("X-Bacon")
                        .descricao("Hambúrguer com bacon")
                        .preco(new BigDecimal("28.90"))
                        .categoria(Categoria.LANCHE)
                        .build(),
                Produto.builder()
                        .id(2L)
                        .nome("X-Salada")
                        .descricao("Hambúrguer com salada")
                        .preco(new BigDecimal("26.90"))
                        .categoria(Categoria.LANCHE)
                        .build()
        );
    }

    @Test
    @DisplayName("Deve retornar lista de produtos da categoria informada")
    void t1() {

        when(produtoRepositorio.buscarPorCategoria(Categoria.LANCHE)).thenReturn(produtosLanche);


        List<ProdutoResponseDTO> resultado = buscarProdutosPorCategoriaService.buscarProdutoPorCategoria(Categoria.LANCHE);


        assertNotNull(resultado, "O resultado não deve ser nulo");
        assertEquals(2, resultado.size(), "Deve retornar 2 produtos");

        // Verifica o primeiro produto
        ProdutoResponseDTO primeiro = resultado.get(0);
        assertEquals(1L, primeiro.id());
        assertEquals("X-Bacon", primeiro.nome());
        assertEquals(new BigDecimal("28.90"), primeiro.preco());
        assertEquals(Categoria.LANCHE, primeiro.categoria());

        // Verifica o segundo produto
        ProdutoResponseDTO segundo = resultado.get(1);
        assertEquals(2L, segundo.id());
        assertEquals("X-Salada", segundo.nome());
        assertEquals(new BigDecimal("26.90"), segundo.preco());
        assertEquals(Categoria.LANCHE, segundo.categoria());

        verify(produtoRepositorio).buscarPorCategoria(Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos na categoria")
    void t2() {

        when(produtoRepositorio.buscarPorCategoria(Categoria.SOBREMESA)).thenReturn(Collections.emptyList());


        List<ProdutoResponseDTO> resultado = buscarProdutosPorCategoriaService.buscarProdutoPorCategoria(Categoria.SOBREMESA);


        assertNotNull(resultado, "O resultado não deve ser nulo");
        assertTrue(resultado.isEmpty(), "A lista deve estar vazia");

        verify(produtoRepositorio).buscarPorCategoria(Categoria.SOBREMESA);
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria é nula")
    void t3() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> buscarProdutosPorCategoriaService.buscarProdutoPorCategoria(null),
                "Deveria lançar exceção para categoria nula");

        assertEquals("Categoria é obrigatória", exception.getMessage(),
                "Mensagem de erro incorreta");

        verify(produtoRepositorio, never()).buscarPorCategoria(any());
    }
}