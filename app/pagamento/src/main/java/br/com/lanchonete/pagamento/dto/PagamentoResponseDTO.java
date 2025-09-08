package br.com.lanchonete.pagamento.dto;

public record PagamentoResponseDTO(
    String pedidoId,
    String status
) {
}