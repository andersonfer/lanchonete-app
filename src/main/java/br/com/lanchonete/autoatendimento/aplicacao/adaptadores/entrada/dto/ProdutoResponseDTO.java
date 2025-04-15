package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

import br.com.lanchonete.autoatendimento.dominio.Categoria;

import java.math.BigDecimal;

public record ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal preco, Categoria categoria) { }