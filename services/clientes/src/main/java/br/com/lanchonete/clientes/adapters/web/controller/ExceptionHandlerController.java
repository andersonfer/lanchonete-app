package br.com.lanchonete.clientes.adapters.web.controller;

import br.com.lanchonete.clientes.adapters.web.dto.ErrorResponse;
import br.com.lanchonete.clientes.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.clientes.domain.exceptions.ValidacaoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErrorResponse> handleValidacaoException(final ValidacaoException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleClienteNaoEncontradoException(final ClienteNaoEncontradoException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erro interno do servidor", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
