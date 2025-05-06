package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

import br.com.lanchonete.autoatendimento.aplicacao.dominio.Categoria;

import java.math.BigDecimal;

public record ProdutoRequestDTO(String nome, String descricao, BigDecimal preco, Categoria categoria) {

}
