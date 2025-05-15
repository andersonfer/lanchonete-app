package br.com.lanchonete.autoatendimento.aplicacao.dto;

import br.com.lanchonete.autoatendimento.dominio.Categoria;

import java.math.BigDecimal;

public record ProdutoRequestDTO(String nome, String descricao, BigDecimal preco, Categoria categoria) {

}
