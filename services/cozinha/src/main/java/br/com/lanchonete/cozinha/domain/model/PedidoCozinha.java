package br.com.lanchonete.cozinha.domain.model;

import java.time.LocalDateTime;

public class PedidoCozinha {
    private Long id;
    private Long pedidoId;
    private StatusPedido status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    public PedidoCozinha() {
    }

    public PedidoCozinha(Long pedidoId) {
        this.pedidoId = pedidoId;
        this.status = StatusPedido.AGUARDANDO;
        this.dataInicio = LocalDateTime.now();
    }

    public void iniciarPreparo() {
        if (this.status != StatusPedido.AGUARDANDO) {
            throw new IllegalStateException("Pedido não está aguardando preparo");
        }
        this.status = StatusPedido.EM_PREPARO;
    }

    public void marcarComoPronto() {
        if (this.status != StatusPedido.EM_PREPARO) {
            throw new IllegalStateException("Pedido não está em preparo");
        }
        this.status = StatusPedido.PRONTO;
        this.dataFim = LocalDateTime.now();
    }

    public void marcarComoRetirado() {
        if (this.status != StatusPedido.PRONTO) {
            throw new IllegalStateException("Pedido não está pronto");
        }
        this.status = StatusPedido.RETIRADO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }
}
