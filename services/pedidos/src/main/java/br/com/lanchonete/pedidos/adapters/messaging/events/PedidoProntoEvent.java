package br.com.lanchonete.pedidos.adapters.messaging.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PedidoProntoEvent implements Serializable {
    private Long pedidoId;
    private Long filaId;
    private LocalDateTime timestamp;

    public PedidoProntoEvent() {}

    public PedidoProntoEvent(Long pedidoId, Long filaId, LocalDateTime timestamp) {
        this.pedidoId = pedidoId;
        this.filaId = filaId;
        this.timestamp = timestamp;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getFilaId() {
        return filaId;
    }

    public void setFilaId(Long filaId) {
        this.filaId = filaId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
