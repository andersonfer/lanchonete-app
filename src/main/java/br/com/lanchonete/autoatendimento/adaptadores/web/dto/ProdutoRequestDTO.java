package br.com.lanchonete.autoatendimento.adaptadores.web.dto;

import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;

import java.math.BigDecimal;

public record ProdutoRequestDTO(String nome, String descricao, BigDecimal preco, Categoria categoria) {

}
