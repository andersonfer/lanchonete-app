package br.com.lanchonete.pagamento.adapters.web.dto;

import java.math.BigDecimal;

public record PagamentoRequest(
        Long pedidoId,
        BigDecimal valor
) {
}
