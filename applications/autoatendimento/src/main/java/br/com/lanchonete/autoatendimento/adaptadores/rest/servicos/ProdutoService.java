package br.com.lanchonete.autoatendimento.adaptadores.rest.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.ProdutoMapper;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
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

    public List<ProdutoResponseDTO> buscarPorCategoria(final Categoria categoria) {
        List<Produto> produtos = buscarProdutosPorCategoria.executar(categoria);
        return produtos.stream()
                .map(produtoMapper::paraDTO)
                .toList();
    }

    @Transactional
    public ProdutoResponseDTO criar(final ProdutoRequestDTO produtoRequest) {
        Produto produtoSalvo = criarProduto.executar(
                produtoRequest.nome(),
                produtoRequest.descricao(),
                produtoRequest.preco(),
                produtoRequest.categoria()
        );
        return produtoMapper.paraDTO(produtoSalvo);
    }

    @Transactional
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

    @Transactional
    public void remover(final Long id) {
        removerProduto.executar(id);
    }
}