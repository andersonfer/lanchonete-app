package br.com.lanchonete.pagamento.dto;

public record WebhookRequestDTO(
    Long pedidoId,
    String statusPagamento
) {
}