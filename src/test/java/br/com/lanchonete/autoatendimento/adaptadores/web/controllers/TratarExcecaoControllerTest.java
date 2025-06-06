package br.com.lanchonete.autoatendimento.adaptadores.web.controllers;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class TratarExcecaoControllerTest {

    private final TratarExcecaoController tratarExcecaoController = new TratarExcecaoController();

    @Test
    @DisplayName("Deve retonar status 400 e uma mensagem de erro para ValidacaoException")
    void t1() {

        String mensagemErro = "Erro de validação";
        ValidacaoException ex = new ValidacaoException(mensagemErro);

        ResponseEntity<String> resposta = tratarExcecaoController.tratarValidacaoException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(mensagemErro, resposta.getBody());

    }
}