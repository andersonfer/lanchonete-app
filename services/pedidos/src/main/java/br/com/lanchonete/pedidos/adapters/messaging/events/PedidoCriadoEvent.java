package br.com.lanchonete.pedidos.adapters.messaging.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PedidoCriadoEvent implements Serializable {
    private Long pedidoId;
    private String valor;
    private String cpfCliente;
    private LocalDateTime timestamp;

    public PedidoCriadoEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public PedidoCriadoEvent(Long pedidoId, String valor, String cpfCliente) {
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.cpfCliente = cpfCliente;
        this.timestamp = LocalDateTime.now();
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
