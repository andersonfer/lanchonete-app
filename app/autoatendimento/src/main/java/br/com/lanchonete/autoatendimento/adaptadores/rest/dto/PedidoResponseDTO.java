package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        String numeroPedido,
        Long clienteId,
        String nomeCliente,
        List<ItemPedidoResponseDTO> itens,
        StatusPedidoDTO status,
        StatusPagamentoDTO statusPagamento,
        LocalDateTime dataCriacao,
        BigDecimal valorTotal
) {
}