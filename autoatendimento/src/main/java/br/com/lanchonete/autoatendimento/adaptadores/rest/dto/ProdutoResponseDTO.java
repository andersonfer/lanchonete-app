package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal preco, CategoriaDTO categoria) {

    public static ProdutoResponseDTO converterParaDTO(Produto produto) {
        return (produto != null)
                ? new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco().getValor(),
                converterCategoriaParaDTO(produto.getCategoria()))
                : null;

    }
    
    private static CategoriaDTO converterCategoriaParaDTO(Categoria categoria) {
        return switch (categoria) {
            case LANCHE -> CategoriaDTO.LANCHE;
            case ACOMPANHAMENTO -> CategoriaDTO.ACOMPANHAMENTO;
            case BEBIDA -> CategoriaDTO.BEBIDA;
            case SOBREMESA -> CategoriaDTO.SOBREMESA;
        };
    }
}