package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;

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
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getNumeroPedido() != null ? pedido.getNumeroPedido().getValor() : null,
                clienteId,
                nomeCliente,
                itensDTO,
                converterStatusPedidoParaDTO(pedido.getStatus()),
                converterStatusPagamentoParaDTO(pedido.getStatusPagamento()),
                pedido.getDataCriacao(),
                pedido.getValorTotal()
        );
    }
    
    private static StatusPagamentoDTO converterStatusPagamentoParaDTO(StatusPagamento statusPagamento) {
        return switch (statusPagamento) {
            case PENDENTE -> StatusPagamentoDTO.PENDENTE;
            case APROVADO -> StatusPagamentoDTO.APROVADO;
            case REJEITADO -> StatusPagamentoDTO.REJEITADO;
        };
    }
    
    private static StatusPedidoDTO converterStatusPedidoParaDTO(StatusPedido statusPedido) {
        return switch (statusPedido) {
            case RECEBIDO -> StatusPedidoDTO.RECEBIDO;
            case EM_PREPARACAO -> StatusPedidoDTO.EM_PREPARACAO;
            case PRONTO -> StatusPedidoDTO.PRONTO;
            case FINALIZADO -> StatusPedidoDTO.FINALIZADO;
        };
    }
}