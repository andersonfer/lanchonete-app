package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

public record ItemPedidoRequestDTO(
        Long produtoId,
        int quantidade
) {}