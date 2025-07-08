package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

public record WebhookPagamentoDTO(
    Long pedidoId,
    String statusPagamento
) {
}