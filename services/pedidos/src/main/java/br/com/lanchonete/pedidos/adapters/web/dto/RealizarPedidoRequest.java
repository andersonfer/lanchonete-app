package br.com.lanchonete.pedidos.adapters.web.dto;

import java.util.List;

public class RealizarPedidoRequest {

    private String cpfCliente;
    private List<ItemPedidoRequest> itens;

    public RealizarPedidoRequest() {}

    public RealizarPedidoRequest(String cpfCliente, List<ItemPedidoRequest> itens) {
        this.cpfCliente = cpfCliente;
        this.itens = itens;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }
}
