package br.com.lanchonete.cozinha.adapters.web.exception;

import br.com.lanchonete.cozinha.domain.exception.PedidoNaoEncontradoException;
import br.com.lanchonete.cozinha.domain.exception.StatusInvalidoException;
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

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handlePedidoNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertNotNull(response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar StatusInvalidoException e retornar 400")
    void t2() {
        StatusInvalidoException exception = new StatusInvalidoException("Status inválido para esta operação");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleStatusInvalido(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Status inválido para esta operação", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException e retornar 400")
    void t3() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Argumento inválido", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException e retornar 400")
    void t4() {
        IllegalStateException exception = new IllegalStateException("Estado inválido");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Estado inválido", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve incluir timestamp nas respostas de erro")
    void t5() {
        PedidoNaoEncontradoException exception = new PedidoNaoEncontradoException(999L);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handlePedidoNaoEncontrado(exception);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("timestamp"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve retornar estrutura correta do body com todos os campos")
    void t6() {
        IllegalArgumentException exception = new IllegalArgumentException("Teste");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(exception);

        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("status"));
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().containsKey("message"));
    }
}
