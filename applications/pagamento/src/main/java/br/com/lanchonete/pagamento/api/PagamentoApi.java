package br.com.lanchonete.pagamento.api;

import br.com.lanchonete.pagamento.dto.PagamentoRequestDTO;
import br.com.lanchonete.pagamento.dto.PagamentoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/pagamentos")
@Tag(name = "Pagamento", description = "Mock do sistema de pagamento Mercado Pago")
public interface PagamentoApi {

    @PostMapping
    @Operation(summary = "Processar pagamento", description = "Inicia o processamento de um pagamento e retorna status PENDENTE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagamento processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<PagamentoResponseDTO> processarPagamento(@RequestBody PagamentoRequestDTO request);
}