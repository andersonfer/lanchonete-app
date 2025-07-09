package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;

public record AtualizarStatusPedidoDTO(
        StatusPedido status
) {
}