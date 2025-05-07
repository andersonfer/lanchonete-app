package br.com.lanchonete.autoatendimento.aplicacao.dto;

public record ItemPedidoDTO(
        Long produtoId,
        int quantidade
) {}