package br.com.lanchonete.cozinha.adapters.messaging.dto;

public class PedidoProntoEvent {
    private Long pedidoId;

    public PedidoProntoEvent() {
    }

    public PedidoProntoEvent(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }
}
