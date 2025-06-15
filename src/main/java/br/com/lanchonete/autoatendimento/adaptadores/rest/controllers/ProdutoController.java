package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.api.ProdutoApi;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.servicos.ProdutoAdaptador;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProdutoController implements ProdutoApi {

    private final ProdutoAdaptador produtoAdaptador;

    public ProdutoController(final ProdutoAdaptador produtoAdaptador) {
        this.produtoAdaptador = produtoAdaptador;
    }

    @Override
    public ResponseEntity<ProdutoResponseDTO> criar(final ProdutoRequestDTO produtoRequest) {
        final ProdutoResponseDTO produto = produtoAdaptador.criar(produtoRequest);
        return new ResponseEntity<>(produto, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProdutoResponseDTO> editar(final Long id, final ProdutoRequestDTO produtoRequest) {
        final ProdutoResponseDTO produto = produtoAdaptador.editar(id, produtoRequest);
        return ResponseEntity.ok(produto);
    }

    @Override
    public ResponseEntity<Void> remover(final Long id) {
        produtoAdaptador.remover(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(final Categoria categoria) {
        final List<ProdutoResponseDTO> produtos = produtoAdaptador.buscarPorCategoria(categoria);
        return ResponseEntity.ok(produtos);
    }
}