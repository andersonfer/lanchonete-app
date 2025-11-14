package br.com.lanchonete.pedidos.adapters.messaging.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PagamentoAprovadoEvent implements Serializable {
    private Long pedidoId;
    private LocalDateTime timestamp;

    public PagamentoAprovadoEvent() {}

    public PagamentoAprovadoEvent(Long pedidoId, LocalDateTime timestamp) {
        this.pedidoId = pedidoId;
        this.timestamp = timestamp;
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
