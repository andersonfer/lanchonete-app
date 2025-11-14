package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.adapters.web.dto.ProdutoResponse;
import br.com.lanchonete.pedidos.application.usecases.ListarProdutosUseCase;
import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {

    @Mock
    private ListarProdutosUseCase listarProdutosUseCase;

    private ProdutoController produtoController;

    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void setUp() {
        produtoController = new ProdutoController(listarProdutosUseCase);

        produto1 = Produto.reconstituir(1L, "X-Burger", "Hambúrguer com queijo", new BigDecimal("15.00"), Categoria.LANCHE);
        produto2 = Produto.reconstituir(2L, "Coca-Cola", "Refrigerante", new BigDecimal("5.00"), Categoria.BEBIDA);
    }

    @Test
    @DisplayName("Deve listar todos os produtos e retornar status 200 OK")
    void t1() {
        List<Produto> produtos = Arrays.asList(produto1, produto2);
        when(listarProdutosUseCase.executar(null)).thenReturn(produtos);

        ResponseEntity<List<ProdutoResponse>> response = produtoController.listarProdutos();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("X-Burger", response.getBody().get(0).getNome());
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals("Coca-Cola", response.getBody().get(1).getNome());
        verify(listarProdutosUseCase, times(1)).executar(null);
    }

    @Test
    @DisplayName("Deve listar produtos por categoria LANCHE e retornar status 200 OK")
    void t2() {
        List<Produto> produtos = Arrays.asList(produto1);
        when(listarProdutosUseCase.executar(Categoria.LANCHE)).thenReturn(produtos);

        ResponseEntity<List<ProdutoResponse>> response = produtoController.listarPorCategoria(Categoria.LANCHE);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("X-Burger", response.getBody().get(0).getNome());
        assertEquals(Categoria.LANCHE, response.getBody().get(0).getCategoria());
        verify(listarProdutosUseCase, times(1)).executar(Categoria.LANCHE);
    }

    @Test
    @DisplayName("Deve listar produtos por categoria BEBIDA e retornar status 200 OK")
    void t3() {
        List<Produto> produtos = Arrays.asList(produto2);
        when(listarProdutosUseCase.executar(Categoria.BEBIDA)).thenReturn(produtos);

        ResponseEntity<List<ProdutoResponse>> response = produtoController.listarPorCategoria(Categoria.BEBIDA);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getId());
        assertEquals("Coca-Cola", response.getBody().get(0).getNome());
        assertEquals(Categoria.BEBIDA, response.getBody().get(0).getCategoria());
        verify(listarProdutosUseCase, times(1)).executar(Categoria.BEBIDA);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void t4() {
        when(listarProdutosUseCase.executar(any())).thenReturn(Arrays.asList());

        ResponseEntity<List<ProdutoResponse>> response = produtoController.listarProdutos();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(listarProdutosUseCase, times(1)).executar(null);
    }
}
