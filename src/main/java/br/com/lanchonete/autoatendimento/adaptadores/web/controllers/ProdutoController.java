package br.com.lanchonete.autoatendimento.adaptadores.web.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.web.api.ProdutoApi;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProdutoController implements ProdutoApi {

    private final CriarProduto criarProduto;
    private final EditarProduto editarProdutoUC;
    private final RemoverProduto removerProdutoUC;
    private final BuscarProdutosPorCategoria buscarProdutosPorCategoria;

    @Override
    public ResponseEntity<ProdutoResponseDTO> criar(final ProdutoRequestDTO produtoRequest) {
        final ProdutoResponseDTO produto = criarProduto.executar(produtoRequest);
        return new ResponseEntity<>(produto, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProdutoResponseDTO> editar(final Long id, final ProdutoRequestDTO produtoRequest) {
        final ProdutoResponseDTO produto = editarProdutoUC.executar(id, produtoRequest);
        return ResponseEntity.ok(produto);
    }

    @Override
    public ResponseEntity<Void> remover(final Long id) {
        removerProdutoUC.executar(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(final Categoria categoria) {
        final List<ProdutoResponseDTO> produtos = buscarProdutosPorCategoria.executar(categoria);
        return ResponseEntity.ok(produtos);
    }
}