package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

public record ItemPedidoDTO(
        Long produtoId,
        int quantidade
) {}