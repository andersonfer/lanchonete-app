package br.com.lanchonete.produtos.adaptadores.dtos;

import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;

import java.math.BigDecimal;

public record ProdutoRequestDTO(String nome, String descricao, BigDecimal preco, CategoriaProduto categoria) {

}