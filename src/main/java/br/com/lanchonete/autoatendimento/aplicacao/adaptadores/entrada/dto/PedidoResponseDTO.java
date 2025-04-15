package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

import br.com.lanchonete.autoatendimento.dominio.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.Pedido;
import br.com.lanchonete.autoatendimento.dominio.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        String nomeCliente,
        List<ItemPedidoResponseDTO> itens,
        StatusPedido status,
        LocalDateTime dataCriacao,
        BigDecimal valorTotal
) {
    public static PedidoResponseDTO converterParaDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        Long clienteId = null;
        String nomeCliente = null;

        if (pedido.getCliente() != null) {
            clienteId = pedido.getCliente().getId();
            nomeCliente = pedido.getCliente().getNome();
        }

        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens().stream()
                .map(ItemPedidoResponseDTO::converterParaDTO)
                .collect(Collectors.toList());

        return new PedidoResponseDTO(
                pedido.getId(),
                clienteId,
                nomeCliente,
                itensDTO,
                pedido.getStatus(),
                pedido.getDataCriacao(),
                pedido.getValorTotal()
        );
    }
}