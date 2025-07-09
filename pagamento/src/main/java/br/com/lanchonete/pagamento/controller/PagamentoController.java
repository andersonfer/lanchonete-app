package br.com.lanchonete.pagamento.controller;

import br.com.lanchonete.pagamento.api.PagamentoApi;
import br.com.lanchonete.pagamento.dto.PagamentoRequestDTO;
import br.com.lanchonete.pagamento.dto.PagamentoResponseDTO;
import br.com.lanchonete.pagamento.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PagamentoController implements PagamentoApi {

    private final PagamentoService pagamentoService;

    public PagamentoController(final PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Override
    public ResponseEntity<PagamentoResponseDTO> processarPagamento(PagamentoRequestDTO request) {
        PagamentoResponseDTO response = pagamentoService.processarPagamento(request);
        return ResponseEntity.ok(response);
    }
}