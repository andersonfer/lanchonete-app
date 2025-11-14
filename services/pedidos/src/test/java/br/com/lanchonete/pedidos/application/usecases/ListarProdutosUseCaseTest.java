package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.ProdutoGateway;
import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Preco;
import br.com.lanchonete.pedidos.domain.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ListarProdutosUseCaseTest {

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private ListarProdutosUseCase listarProdutosUseCase;

    private Produto lanche1;
    private Produto lanche2;
    private Produto bebida1;

    @BeforeEach
    void configurar() {
        lanche1 = Produto.reconstituir(
                1L,
                "X-Burger",
                "Hambúrguer com queijo",
                new BigDecimal("15.00"),
                Categoria.LANCHE
        );

        lanche2 = Produto.reconstituir(
                2L,
                "X-Salad",
                "Hambúrguer com salada",
                new BigDecimal("18.00"),
                Categoria.LANCHE
        );

        bebida1 = Produto.reconstituir(
                3L,
                "Coca-Cola",
                "Refrigerante",
                new BigDecimal("5.00"),
                Categoria.BEBIDA
        );
    }

    @Test
    @DisplayName("Deve listar todos os produtos quando categoria é nula")
    void t1() {
        List<Produto> todosProdutos = Arrays.asList(lanche1, lanche2, bebida1);
        when(produtoGateway.listarTodos()).thenReturn(todosProdutos);

        List<Produto> resultado = listarProdutosUseCase.executar(null);

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        verify(produtoGateway, times(1)).listarTodos();
        verify(produtoGateway, never()).listarPorCategoria(any());
    }

    @Test
    @DisplayName("Deve listar produtos filtrados por categoria LANCHE")
    void t2() {
        List<Produto> lanches = Arrays.asList(lanche1, lanche2);
        when(produtoGateway.listarPorCategoria(Categoria.LANCHE)).thenReturn(lanches);

        List<Produto> resultado = listarProdutosUseCase.executar(Categoria.LANCHE);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getCategoria() == Categoria.LANCHE));
        verify(produtoGateway, times(1)).listarPorCategoria(Categoria.LANCHE);
        verify(produtoGateway, never()).listarTodos();
    }

    @Test
    @DisplayName("Deve listar produtos filtrados por categoria BEBIDA")
    void t3() {
        List<Produto> bebidas = Arrays.asList(bebida1);
        when(produtoGateway.listarPorCategoria(Categoria.BEBIDA)).thenReturn(bebidas);

        List<Produto> resultado = listarProdutosUseCase.executar(Categoria.BEBIDA);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(Categoria.BEBIDA, resultado.get(0).getCategoria());
        verify(produtoGateway, times(1)).listarPorCategoria(Categoria.BEBIDA);
        verify(produtoGateway, never()).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos na categoria")
    void t4() {
        when(produtoGateway.listarPorCategoria(Categoria.ACOMPANHAMENTO)).thenReturn(List.of());

        List<Produto> resultado = listarProdutosUseCase.executar(Categoria.ACOMPANHAMENTO);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(produtoGateway, times(1)).listarPorCategoria(Categoria.ACOMPANHAMENTO);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos cadastrados")
    void t5() {
        when(produtoGateway.listarTodos()).thenReturn(List.of());

        List<Produto> resultado = listarProdutosUseCase.executar(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(produtoGateway, times(1)).listarTodos();
    }
}
