package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

public record WebhookPagamentoRequestDTO(
    Long pedidoId,
    String statusPagamento
) {
}