package br.com.lanchonete.pagamento.adapters.messaging;

import java.math.BigDecimal;

public record PedidoCriadoEvent(
        Long pedidoId,
        BigDecimal valor,
        String cpf
) {
}
