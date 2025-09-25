package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

// Classe para informações do item do pedido - com pipeline corrigido
public record ItemPedidoInfo(Long produtoId, int quantidade) {}