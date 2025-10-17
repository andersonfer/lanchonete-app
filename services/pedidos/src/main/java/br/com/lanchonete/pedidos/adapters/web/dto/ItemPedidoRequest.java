package br.com.lanchonete.pedidos.adapters.web.dto;

public class ItemPedidoRequest {

    private Long produtoId;
    private int quantidade;

    public ItemPedidoRequest() {}

    public ItemPedidoRequest(Long produtoId, int quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
