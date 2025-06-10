package br.com.lanchonete.autoatendimento.controllers.dto;

public record ItemPedidoDTO(
        Long produtoId,
        int quantidade
) {}