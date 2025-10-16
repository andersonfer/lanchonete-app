package br.com.lanchonete.clientes.adapters.web.controller;

import br.com.lanchonete.clientes.adapters.web.dto.ErrorResponse;
import br.com.lanchonete.clientes.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.clientes.domain.exceptions.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerControllerTest {

    private ExceptionHandlerController exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ExceptionHandlerController();
    }

    @Test
    @DisplayName("Deve retornar 400 BAD_REQUEST ao tratar ValidacaoException")
    void t1() {
        ValidacaoException exception = new ValidacaoException("CPF inválido");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidacaoException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CPF inválido", response.getBody().mensagem());
        assertEquals(400, response.getBody().status());
    }

    @Test
    @DisplayName("Deve retornar 404 NOT_FOUND ao tratar ClienteNaoEncontradoException")
    void t2() {
        String cpf = "12345678901";
        ClienteNaoEncontradoException exception = new ClienteNaoEncontradoException(cpf);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleClienteNaoEncontradoException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cliente não encontrado com CPF: " + cpf, response.getBody().mensagem());
        assertEquals(404, response.getBody().status());
    }

    @Test
    @DisplayName("Deve retornar 500 INTERNAL_SERVER_ERROR ao tratar Exception genérica")
    void t3() {
        Exception exception = new RuntimeException("Erro inesperado");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Erro interno do servidor", response.getBody().mensagem());
        assertEquals(500, response.getBody().status());
    }
}
