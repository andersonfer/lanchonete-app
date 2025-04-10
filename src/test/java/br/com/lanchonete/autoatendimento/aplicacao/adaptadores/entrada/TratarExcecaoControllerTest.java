package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class TratarExcecaoControllerTest {

    private final TratarExcecaoController tratarExcecaoController = new TratarExcecaoController();

    @Test
    @DisplayName("Deve retonar status 400 e uma mensagem de erro para IllegalArgumentException")
    void t1() {

        String mensagemErro = "Erro de validação";
        IllegalArgumentException ex = new IllegalArgumentException(mensagemErro);

        ResponseEntity<String> resposta = tratarExcecaoController.tratarIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(mensagemErro, resposta.getBody());

    }
}