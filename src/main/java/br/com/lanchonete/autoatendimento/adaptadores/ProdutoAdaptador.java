package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoAdaptador {

    private final BuscarProdutosPorCategoria buscarProdutosPorCategoria;
    private final CriarProduto criarProduto;
    private final EditarProduto editarProduto;
    private final RemoverProduto removerProduto;

    public List<ProdutoResponseDTO> buscarPorCategoria(final Categoria categoria) {
        return buscarProdutosPorCategoria.executar(categoria);
    }

    @Transactional
    public ProdutoResponseDTO criar(final ProdutoRequestDTO produtoRequest) {
        return criarProduto.executar(produtoRequest);
    }

    @Transactional
    public ProdutoResponseDTO editar(final Long id, final ProdutoRequestDTO produtoRequest) {
        return editarProduto.executar(id, produtoRequest);
    }

    @Transactional
    public void remover(final Long id) {
        removerProduto.executar(id);
    }
}