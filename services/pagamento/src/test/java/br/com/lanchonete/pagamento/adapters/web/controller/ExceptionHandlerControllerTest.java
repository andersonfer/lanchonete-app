package br.com.lanchonete.pagamento.adapters.web.controller;

import br.com.lanchonete.pagamento.adapters.web.dto.ErrorResponse;
import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerControllerTest {

    private ExceptionHandlerController exceptionHandler;

    @BeforeEach
    void configurar() {
        exceptionHandler = new ExceptionHandlerController();
    }

    @Test
    @DisplayName("Deve retornar 400 BAD_REQUEST ao tratar ValidacaoException")
    void t1() {
        ValidacaoException exception = new ValidacaoException("Valor inválido");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidacaoException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Valor inválido", response.getBody().mensagem());
    }

    @Test
    @DisplayName("Deve retornar 500 INTERNAL_SERVER_ERROR ao tratar Exception genérica")
    void t2() {
        Exception exception = new Exception("Erro inesperado");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Erro interno do servidor", response.getBody().mensagem());
    }

    @Test
    @DisplayName("Deve retornar mensagem correta da exceção")
    void t3() {
        String mensagemEsperada = "PedidoId não pode ser nulo";
        ValidacaoException exception = new ValidacaoException(mensagemEsperada);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidacaoException(exception);

        assertEquals(mensagemEsperada, response.getBody().mensagem());
    }
}
