package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TratarExcecaoController {

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<String> tratarValidacaoException(ValidacaoException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
