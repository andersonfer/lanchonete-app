package br.com.lanchonete.autoatendimento.dominio;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data

public class Pedido {
    private Long id;
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


}