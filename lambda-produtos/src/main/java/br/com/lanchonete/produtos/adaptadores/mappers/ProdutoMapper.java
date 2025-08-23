package br.com.lanchonete.produtos.adaptadores.mappers;

import br.com.lanchonete.produtos.adaptadores.dtos.ProdutoResponseDTO;
import br.com.lanchonete.produtos.dominio.entidades.Produto;

public class ProdutoMapper implements Mapper<Produto, ProdutoResponseDTO> {

    private final EnumsMapper enumsMapper;

    public ProdutoMapper(EnumsMapper enumsMapper) {
        this.enumsMapper = enumsMapper;
    }

    @Override
    public ProdutoResponseDTO paraDTO(Produto produto) {
        if (produto == null) {
            return null;
        }

        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco().getValor(),
                enumsMapper.categoriaParaDTO(produto.getCategoria())
        );
    }

    @Override
    public Produto paraDominio(ProdutoResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        return Produto.reconstituir(
                dto.id(),
                dto.nome(),
                dto.descricao(),
                dto.preco(),
                enumsMapper.categoriaParaDominio(dto.categoria())
        );
    }
}