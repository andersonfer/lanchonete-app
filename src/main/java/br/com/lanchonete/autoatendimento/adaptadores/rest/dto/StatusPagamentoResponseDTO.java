package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;

public record StatusPagamentoResponseDTO(
        Long pedidoId,
        StatusPagamento statusPagamento,
        String mensagem
) {

    public static StatusPagamentoResponseDTO pendente(Long pedidoId) {
        return new StatusPagamentoResponseDTO(
                pedidoId,
                StatusPagamento.PENDENTE,
                "Pagamento pendente de processamento"
        );
    }

    public static StatusPagamentoResponseDTO aprovado(Long pedidoId) {
        return new StatusPagamentoResponseDTO(
                pedidoId,
                StatusPagamento.APROVADO,
                "Pagamento aprovado com sucesso"
        );
    }

    public static StatusPagamentoResponseDTO rejeitado(Long pedidoId) {
        return new StatusPagamentoResponseDTO(
                pedidoId,
                StatusPagamento.REJEITADO,
                "Pagamento rejeitado"
        );
    }

    public static StatusPagamentoResponseDTO de(Long pedidoId, StatusPagamento statusPagamento) {
        return switch (statusPagamento) {
            case PENDENTE -> pendente(pedidoId);
            case APROVADO -> aprovado(pedidoId);
            case REJEITADO -> rejeitado(pedidoId);
        };
    }
}