package br.com.lanchonete.autoatendimento.aplicacao.dto;

import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal preco, Categoria categoria) {

    public static ProdutoResponseDTO converterParaDTO(Produto produto) {
        return (produto != null)
                ? new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria())
                : null;

    }
}