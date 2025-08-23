package br.com.lanchonete.produtos.adaptadores.mappers;

import br.com.lanchonete.produtos.adaptadores.dtos.CategoriaDTO;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;

public class EnumsMapper {

    public CategoriaDTO categoriaParaDTO(CategoriaProduto categoria) {
        return switch (categoria) {
            case LANCHE -> CategoriaDTO.LANCHE;
            case ACOMPANHAMENTO -> CategoriaDTO.ACOMPANHAMENTO;
            case BEBIDA -> CategoriaDTO.BEBIDA;
            case SOBREMESA -> CategoriaDTO.SOBREMESA;
        };
    }

    public CategoriaProduto categoriaParaDominio(CategoriaDTO categoriaDTO) {
        return switch (categoriaDTO) {
            case LANCHE -> CategoriaProduto.LANCHE;
            case ACOMPANHAMENTO -> CategoriaProduto.ACOMPANHAMENTO;
            case BEBIDA -> CategoriaProduto.BEBIDA;
            case SOBREMESA -> CategoriaProduto.SOBREMESA;
        };
    }
}