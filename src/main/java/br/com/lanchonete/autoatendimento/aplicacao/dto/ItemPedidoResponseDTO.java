package br.com.lanchonete.autoatendimento.aplicacao.dto;

import br.com.lanchonete.autoatendimento.dominio.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
        Long id,
        Long produtoId,
        String nomeProduto,
        String descricaoProduto,
        int quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {
    public static ItemPedidoResponseDTO converterParaDTO(ItemPedido item) {
        return (item != null)
                ? new ItemPedidoResponseDTO(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getProduto().getDescricao(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getValorTotal())
                : null;
    }
}