package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoAdaptador {

    private final BuscarProdutosPorCategoria buscarProdutosPorCategoria;
    private final CriarProduto criarProduto;
    private final EditarProduto editarProduto;
    private final RemoverProduto removerProduto;

    public ProdutoAdaptador(final BuscarProdutosPorCategoria buscarProdutosPorCategoria,
                           final CriarProduto criarProduto,
                           final EditarProduto editarProduto,
                           final RemoverProduto removerProduto) {
        this.buscarProdutosPorCategoria = buscarProdutosPorCategoria;
        this.criarProduto = criarProduto;
        this.editarProduto = editarProduto;
        this.removerProduto = removerProduto;
    }

    public List<ProdutoResponseDTO> buscarPorCategoria(final Categoria categoria) {
        List<Produto> produtos = buscarProdutosPorCategoria.executar(categoria);
        return produtos.stream()
                .map(ProdutoResponseDTO::converterParaDTO)
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
        return ProdutoResponseDTO.converterParaDTO(produtoSalvo);
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
        return ProdutoResponseDTO.converterParaDTO(produtoAtualizado);
    }

    @Transactional
    public void remover(final Long id) {
        removerProduto.executar(id);
    }
}