package br.com.lanchonete.produtos.adaptadores.dtos;

import java.math.BigDecimal;

public record ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal preco, CategoriaDTO categoria) {
}