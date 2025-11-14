package br.com.lanchonete.cozinha.adapters.messaging.dto;

public class PedidoRetiradoEvent {
    private Long pedidoId;

    public PedidoRetiradoEvent() {
    }

    public PedidoRetiradoEvent(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }
}
