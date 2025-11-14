package br.com.lanchonete.cozinha.adapters.messaging.dto;

public class PagamentoAprovadoEvent {
    private Long pedidoId;

    public PagamentoAprovadoEvent() {
    }

    public PagamentoAprovadoEvent(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }
}
