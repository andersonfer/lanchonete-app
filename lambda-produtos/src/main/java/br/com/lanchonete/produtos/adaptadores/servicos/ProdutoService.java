package br.com.lanchonete.produtos.adaptadores.servicos;

import br.com.lanchonete.produtos.adaptadores.dtos.ProdutoRequestDTO;
import br.com.lanchonete.produtos.adaptadores.dtos.ProdutoResponseDTO;
import br.com.lanchonete.produtos.adaptadores.mappers.ProdutoMapper;
import br.com.lanchonete.produtos.aplicacao.casosdeuso.BuscarProdutosPorCategoria;
import br.com.lanchonete.produtos.aplicacao.casosdeuso.CriarProduto;
import br.com.lanchonete.produtos.aplicacao.casosdeuso.EditarProduto;
import br.com.lanchonete.produtos.aplicacao.casosdeuso.RemoverProduto;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;
import br.com.lanchonete.produtos.dominio.entidades.Produto;

import java.util.List;

public class ProdutoService {

    private final BuscarProdutosPorCategoria buscarProdutosPorCategoria;
    private final CriarProduto criarProduto;
    private final EditarProduto editarProduto;
    private final RemoverProduto removerProduto;
    private final ProdutoMapper produtoMapper;

    public ProdutoService(final BuscarProdutosPorCategoria buscarProdutosPorCategoria,
                          final CriarProduto criarProduto,
                          final EditarProduto editarProduto,
                          final RemoverProduto removerProduto,
                          final ProdutoMapper produtoMapper) {
        this.buscarProdutosPorCategoria = buscarProdutosPorCategoria;
        this.criarProduto = criarProduto;
        this.editarProduto = editarProduto;
        this.removerProduto = removerProduto;
        this.produtoMapper = produtoMapper;
    }

    public List<ProdutoResponseDTO> buscarPorCategoria(final CategoriaProduto categoria) {
        List<Produto> produtos = buscarProdutosPorCategoria.executar(categoria);
        return produtos.stream()
                .map(produtoMapper::paraDTO)
                .toList();
    }

    public ProdutoResponseDTO criar(final ProdutoRequestDTO produtoRequest) {
        Produto produtoSalvo = criarProduto.executar(
                produtoRequest.nome(),
                produtoRequest.descricao(),
                produtoRequest.preco(),
                produtoRequest.categoria()
        );
        return produtoMapper.paraDTO(produtoSalvo);
    }

    public ProdutoResponseDTO editar(final Long id, final ProdutoRequestDTO produtoRequest) {
        Produto produtoAtualizado = editarProduto.executar(
                id,
                produtoRequest.nome(),
                produtoRequest.descricao(),
                produtoRequest.preco(),
                produtoRequest.categoria()
        );
        return produtoMapper.paraDTO(produtoAtualizado);
    }

    public void remover(final Long id) {
        removerProduto.executar(id);
    }
}