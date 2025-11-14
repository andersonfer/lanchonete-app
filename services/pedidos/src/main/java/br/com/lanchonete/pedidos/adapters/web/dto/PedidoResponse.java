package br.com.lanchonete.pedidos.adapters.web.dto;

import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoResponse {
    private Long id;
    private String numeroPedido;
    private String cpfCliente;
    private String clienteNome;
    private StatusPedido status;
    private BigDecimal valorTotal;
    private List<ItemPedidoResponse> itens;
    private LocalDateTime dataCriacao;

    public PedidoResponse() {}

    public static PedidoResponse fromDomain(Pedido pedido) {
        PedidoResponse response = new PedidoResponse();
        response.setId(pedido.getId());
        response.setNumeroPedido(pedido.getNumeroPedido() != null ? pedido.getNumeroPedido().getValor() : null);
        response.setCpfCliente(pedido.getCpfCliente());
        response.setClienteNome(pedido.getClienteNome());
        response.setStatus(pedido.getStatus());
        response.setValorTotal(pedido.getValorTotal());
        response.setDataCriacao(pedido.getDataCriacao());

        if (pedido.getItens() != null) {
            response.setItens(pedido.getItens().stream()
                    .map(ItemPedidoResponse::fromDomain)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemPedidoResponse> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResponse> itens) {
        this.itens = itens;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
