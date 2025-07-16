package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;

public record AtualizarStatusPedidoDTO(
        StatusPedidoDTO status
) {
    
    public StatusPedido converterParaDominio() {
        return switch (status) {
            case RECEBIDO -> StatusPedido.RECEBIDO;
            case EM_PREPARACAO -> StatusPedido.EM_PREPARACAO;
            case PRONTO -> StatusPedido.PRONTO;
            case FINALIZADO -> StatusPedido.FINALIZADO;
        };
    }
}