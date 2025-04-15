package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

public record ItemPedidoDTO(
        Long produtoId,
        int quantidade
) {}