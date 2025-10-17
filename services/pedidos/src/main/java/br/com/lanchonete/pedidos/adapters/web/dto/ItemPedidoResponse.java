package br.com.lanchonete.pedidos.adapters.web.dto;

import br.com.lanchonete.pedidos.domain.model.ItemPedido;

import java.math.BigDecimal;

public class ItemPedidoResponse {
    private String produtoNome;
    private int quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;

    public ItemPedidoResponse() {}

    public static ItemPedidoResponse fromDomain(ItemPedido item) {
        ItemPedidoResponse response = new ItemPedidoResponse();
        response.setProdutoNome(item.getProduto().getNome());
        response.setQuantidade(item.getQuantidade());
        response.setValorUnitario(item.getValorUnitario());
        response.setValorTotal(item.getValorTotal());
        return response;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome = produtoNome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
