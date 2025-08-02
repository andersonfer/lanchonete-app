package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto;

import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
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
class BuscarProdutosPorCategoriaTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private BuscarProdutosPorCategoria buscarProdutosPorCategoria;

    private List<Produto> produtosLanche;

    @BeforeEach
    void configurar() {
        produtosLanche = Arrays.asList(
                Produto.reconstituir(1L, "X-Bacon", "Hambúrguer com bacon",
                        new BigDecimal("28.90"), Categoria.LANCHE),
                Produto.reconstituir(2L, "X-Salada", "Hambúrguer com salada",
                        new BigDecimal("26.90"), Categoria.LANCHE)
        );
    }

    @Test
    @DisplayName("Deve retornar lista de produtos da categoria informada")
    void t1() {

        when(produtoGateway.buscarPorCategoria(Categoria.LANCHE)).thenReturn(produtosLanche);


        List<Produto> resultado = buscarProdutosPorCategoria.executar(Categoria.LANCHE);


        assertNotNull(resultado, "O resultado não deve ser nulo");
        assertEquals(2, resultado.size(), "Deve retornar 2 produtos");

        // Verifica o primeiro produto
        Produto primeiro = resultado.get(0);
        assertEquals(1L, primeiro.getId());
        assertEquals("X-Bacon", primeiro.getNome());
        assertEquals(new BigDecimal("28.90"), primeiro.getPreco().getValor());
        assertEquals(Categoria.LANCHE, primeiro.getCategoria());

        // Verifica o segundo produto
        Produto segundo = resultado.get(1);
        assertEquals(2L, segundo.getId());
        assertEquals("X-Salada", segundo.getNome());
        assertEquals(new BigDecimal("26.90"), segundo.getPreco().getValor());
        assertEquals(Categoria.LANCHE, segundo.getCategoria());

        verify(produtoGateway).buscarPorCategoria(Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos na categoria")
    void t2() {

        when(produtoGateway.buscarPorCategoria(Categoria.SOBREMESA)).thenReturn(Collections.emptyList());


        List<Produto> resultado = buscarProdutosPorCategoria.executar(Categoria.SOBREMESA);


        assertNotNull(resultado, "O resultado não deve ser nulo");
        assertTrue(resultado.isEmpty(), "A lista deve estar vazia");

        verify(produtoGateway).buscarPorCategoria(Categoria.SOBREMESA);
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria é nula")
    void t3() {

        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> buscarProdutosPorCategoria.executar(null),
                "Deveria lançar exceção para categoria nula");

        assertEquals("Categoria é obrigatória", exception.getMessage(),
                "Mensagem de erro incorreta");

        verify(produtoGateway, never()).buscarPorCategoria(any());
    }
}