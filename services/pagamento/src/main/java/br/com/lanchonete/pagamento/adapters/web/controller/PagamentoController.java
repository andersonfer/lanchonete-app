package br.com.lanchonete.pagamento.adapters.web.controller;

import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoRequest;
import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoResponse;
import br.com.lanchonete.pagamento.adapters.web.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
@Tag(name = "Pagamentos", description = "Endpoints para processamento de pagamentos")
public class PagamentoController {

    private static final Logger log = LoggerFactory.getLogger(PagamentoController.class);

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Operation(
            summary = "Processar pagamento",
            description = "Processa um pagamento de pedido (mock que sempre aprova automaticamente)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento processado com sucesso",
                    content = @Content(schema = @Schema(implementation = PagamentoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de pagamento inválidos"
            )
    })
    @PostMapping
    public ResponseEntity<PagamentoResponse> processar(@RequestBody PagamentoRequest request) {
        log.info("CD Versionado SHA - Pagamento solicitado para pedido: {}", request.pedidoId());
        PagamentoResponse response = pagamentoService.processar(request);
        return ResponseEntity.ok(response);
    }
}
