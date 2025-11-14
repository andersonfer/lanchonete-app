package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.exceptions.PedidoNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.exceptions.ProdutoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar PedidoNaoEncontradoException e retornar 404")
    void t1() {
        PedidoNaoEncontradoException exception = new PedidoNaoEncontradoException(1L);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handlePedidoNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Pedido não encontrado: 1", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Deve tratar ProdutoNaoEncontradoException e retornar 404")
    void t2() {
        ProdutoNaoEncontradoException exception = new ProdutoNaoEncontradoException(5L);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleProdutoNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Produto não encontrado: 5", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Deve tratar ClienteNaoEncontradoException e retornar 404")
    void t3() {
        ClienteNaoEncontradoException exception = new ClienteNaoEncontradoException("12345678901");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleClienteNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Cliente não encontrado: 12345678901", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException e retornar 400")
    void t4() {
        IllegalArgumentException exception = new IllegalArgumentException("Dados inválidos");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Dados inválidos", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Deve tratar Exception genérica e retornar 500")
    void t5() {
        Exception exception = new Exception("Erro inesperado");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().containsKey("details"));
        assertEquals("Erro interno do servidor", response.getBody().get("error"));
        assertEquals("Erro inesperado", response.getBody().get("details"));
    }

    @Test
    @DisplayName("Deve tratar RuntimeException genérica e retornar 500")
    void t6() {
        RuntimeException exception = new RuntimeException("Falha no processamento");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro interno do servidor", response.getBody().get("error"));
        assertEquals("Falha no processamento", response.getBody().get("details"));
    }
}
