package br.com.lanchonete.autoatendimento.dominio.modelo.pedido;

import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import java.math.BigDecimal;
import java.util.Objects;

public class ItemPedido {
    private Long id;
    private Pedido pedido;
    private Produto produto;
    private int quantidade;
    private BigDecimal valorUnitario;  // Armazena o valor no momento da compra
    private BigDecimal valorTotal;


    // Construtor privado para criação - novos itens com validações
    private ItemPedido(Produto produto, int quantidade, BigDecimal valorUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        calcularValorTotal();
    }

    // Construtor privado para reconstituição - dados já validados do banco
    private ItemPedido(Long id, Pedido pedido, Produto produto, int quantidade, 
                       BigDecimal valorUnitario, BigDecimal valorTotal) {
        this.id = id;
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
    }

    public static ItemPedido criar(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        
        return new ItemPedido(produto, quantidade, produto.getPreco().getValor());
    }

    public static ItemPedido reconstituir(Long id, Pedido pedido, Produto produto, int quantidade, 
                                          BigDecimal valorUnitario, BigDecimal valorTotal) {
        return new ItemPedido(id, pedido, produto, quantidade, valorUnitario, valorTotal);
    }

    public void calcularValorTotal() {
        if (this.valorUnitario != null && this.quantidade > 0) {
            this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ItemPedido itemPedido = (ItemPedido) obj;
        return quantidade == itemPedido.quantidade &&
                Objects.equals(id, itemPedido.id) &&
                Objects.equals(pedido, itemPedido.pedido) &&
                Objects.equals(produto, itemPedido.produto) &&
                Objects.equals(valorUnitario, itemPedido.valorUnitario) &&
                Objects.equals(valorTotal, itemPedido.valorTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pedido, produto, quantidade, valorUnitario, valorTotal);
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "id=" + id +
                ", pedido=" + pedido +
                ", produto=" + produto +
                ", quantidade=" + quantidade +
                ", valorUnitario=" + valorUnitario +
                ", valorTotal=" + valorTotal +
                '}';
    }
}