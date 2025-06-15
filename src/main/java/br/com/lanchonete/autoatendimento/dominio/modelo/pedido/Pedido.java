package br.com.lanchonete.autoatendimento.dominio.modelo.pedido;

import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.shared.NumeroPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {
    private Long id;
    private NumeroPedido numeroPedido;
    private Cliente cliente;  // Opcional - cliente pode não se identificar
    private List<ItemPedido> itens;
    private StatusPedido status;
    private LocalDateTime dataCriacao;
    private BigDecimal valorTotal;

    private Pedido(Cliente cliente, StatusPedido status, LocalDateTime dataCriacao) {
        this.cliente = cliente;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.itens = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
    }

    public static Pedido criar(Cliente cliente, StatusPedido status, LocalDateTime dataCriacao) {
        return new Pedido(cliente, status, dataCriacao);
    }

    public void calcularValorTotal() {
        if (itens == null || itens.isEmpty()) {
            this.valorTotal = BigDecimal.ZERO;
            return;
        }

        this.valorTotal = itens.stream()
                .map(ItemPedido::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void adicionarItem(ItemPedido item) {
        if (this.itens == null) {
            this.itens = new ArrayList<>();
        }
        this.itens.add(item);
        item.setPedido(this);
        calcularValorTotal();
    }

    public void setId(Long id) {
        this.id = id;
        if (id != null) {
            this.numeroPedido = NumeroPedido.gerarPorId(id);
        }
    }


    public void validar() {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter pelo menos um item");
        }

        validarItens();

        if (status == null) {
            throw new IllegalArgumentException("Status do pedido é obrigatório");
        }

        if (dataCriacao == null) {
            throw new IllegalArgumentException("Data de criação é obrigatória");
        }
    }

    private void validarItens() {
        if (itens != null) {
            for (ItemPedido item : itens) {
                validarItem(item);
            }
        }
    }

    private void validarItem(ItemPedido item) {
        if (item == null) {
            throw new IllegalArgumentException("Item não pode ser nulo");
        }

        if (item.getProduto() == null) {
            throw new IllegalArgumentException("Produto do item é obrigatório");
        }

        if (item.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        if (item.getValorUnitario() == null) {
            throw new IllegalArgumentException("Valor unitário é obrigatório");
        }

        if (item.getValorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor unitário deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public NumeroPedido getNumeroPedido() {
        return numeroPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
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
        Pedido pedido = (Pedido) obj;
        return Objects.equals(id, pedido.id) &&
                Objects.equals(numeroPedido, pedido.numeroPedido) &&
                Objects.equals(cliente, pedido.cliente) &&
                Objects.equals(itens, pedido.itens) &&
                status == pedido.status &&
                Objects.equals(dataCriacao, pedido.dataCriacao) &&
                Objects.equals(valorTotal, pedido.valorTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroPedido, cliente, itens, status, dataCriacao, valorTotal);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", numeroPedido=" + numeroPedido +
                ", cliente=" + cliente +
                ", itens=" + itens +
                ", status=" + status +
                ", dataCriacao=" + dataCriacao +
                ", valorTotal=" + valorTotal +
                '}';
    }
}