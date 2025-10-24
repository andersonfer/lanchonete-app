package br.com.lanchonete.cozinha.adapters.web.dto;

import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.model.StatusPedido;

import java.time.LocalDateTime;

public class PedidoCozinhaResponse {
    private Long id;
    private Long pedidoId;
    private StatusPedido status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    public PedidoCozinhaResponse() {
    }

    public static PedidoCozinhaResponse fromDomain(PedidoCozinha pedido) {
        PedidoCozinhaResponse response = new PedidoCozinhaResponse();
        response.setId(pedido.getId());
        response.setPedidoId(pedido.getPedidoId());
        response.setStatus(pedido.getStatus());
        response.setDataInicio(pedido.getDataInicio());
        response.setDataFim(pedido.getDataFim());
        return response;
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
