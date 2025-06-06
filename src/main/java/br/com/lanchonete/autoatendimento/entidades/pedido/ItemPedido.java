package br.com.lanchonete.autoatendimento.entidades.pedido;

import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemPedido {
    private Long id;
    private Pedido pedido;
    private Produto produto;
    private int quantidade;
    private BigDecimal valorUnitario;  // Armazena o valor no momento da compra
    private BigDecimal valorTotal;

    public void calcularValorTotal() {
        if (this.valorUnitario != null && this.quantidade > 0) {
            this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }
}