package br.com.lanchonete.pedidos.adapters.messaging.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PedidoRetiradoEvent implements Serializable {
    private Long pedidoId;
    private LocalDateTime timestamp;

    public PedidoRetiradoEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public PedidoRetiradoEvent(Long pedidoId) {
        this.pedidoId = pedidoId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
