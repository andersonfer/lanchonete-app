package br.com.lanchonete.pagamento.adapters.web.dto;

public record PagamentoResponse(
        String id,
        Long pedidoId,
        String status
) {
}
