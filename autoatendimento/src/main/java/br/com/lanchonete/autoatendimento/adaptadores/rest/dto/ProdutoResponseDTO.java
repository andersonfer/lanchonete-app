package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal preco, CategoriaDTO categoria) {
}