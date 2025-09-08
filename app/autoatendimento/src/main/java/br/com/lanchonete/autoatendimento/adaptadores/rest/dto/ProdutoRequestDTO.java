package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;

import java.math.BigDecimal;

public record ProdutoRequestDTO(String nome, String descricao, BigDecimal preco, Categoria categoria) {

}
