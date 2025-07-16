package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.api.ProdutoApi;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.servicos.ProdutoService;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProdutoController implements ProdutoApi {

    private final ProdutoService produtoService;

    public ProdutoController(final ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Override
    public ResponseEntity<ProdutoResponseDTO> criar(final ProdutoRequestDTO produtoRequest) {
        final ProdutoResponseDTO produto = produtoService.criar(produtoRequest);
        return new ResponseEntity<>(produto, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProdutoResponseDTO> editar(final Long id, final ProdutoRequestDTO produtoRequest) {
        final ProdutoResponseDTO produto = produtoService.editar(id, produtoRequest);
        return ResponseEntity.ok(produto);
    }

    @Override
    public ResponseEntity<Void> remover(final Long id) {
        produtoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(final Categoria categoria) {
        final List<ProdutoResponseDTO> produtos = produtoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(produtos);
    }
}