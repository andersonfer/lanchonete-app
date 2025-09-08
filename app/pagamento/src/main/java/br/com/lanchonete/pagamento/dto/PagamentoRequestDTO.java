package br.com.lanchonete.pagamento.dto;

import java.math.BigDecimal;

public record PagamentoRequestDTO(
    String pedidoId,
    BigDecimal valor
) {
}