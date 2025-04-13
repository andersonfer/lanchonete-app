package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.Produto;

public class ProdutoMapper {

    private ProdutoMapper() {};

    public static ProdutoResponseDTO converterParaResponseDTO(Produto produto) {
        if (produto == null) {
            return null;
        }

        return ProdutoResponseDTO.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .preco(produto.getPreco())
                .categoria(produto.getCategoria())
                .build();
    }
}
