package br.com.lanchonete.autoatendimento.dominio.modelo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Pedido {
    private Long id;
    private Cliente cliente;  // Opcional - cliente pode não se identificar
    private List<ItemPedido> itens;
    private StatusPedido status;
    private LocalDateTime dataCriacao;
    private BigDecimal valorTotal;

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
}