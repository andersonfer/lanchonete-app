package br.com.lanchonete.pagamento.adapters.web.controller;

import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoRequest;
import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoResponse;
import br.com.lanchonete.pagamento.adapters.web.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> processar(@RequestBody PagamentoRequest request) {
        PagamentoResponse response = pagamentoService.processar(request);
        return ResponseEntity.ok(response);
    }
}
