package br.com.lanchonete.cozinha.adapters.web.dto;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;

import java.time.LocalDateTime;

public record PedidoCozinhaResponse(
    Long id,
    Long pedidoId,
    StatusPedido status,
    LocalDateTime dataInicio,
    LocalDateTime dataFim
) {
    public static PedidoCozinhaResponse fromDomain(PedidoCozinha pedido) {
        return new PedidoCozinhaResponse(
            pedido.getId(),
            pedido.getPedidoId(),
            pedido.getStatus(),
            pedido.getDataInicio(),
            pedido.getDataFim()
        );
    }
}
