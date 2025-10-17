package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.adapters.web.dto.ProdutoResponse;
import br.com.lanchonete.pedidos.application.usecases.ListarProdutosUseCase;
import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ListarProdutosUseCase listarProdutosUseCase;

    public ProdutoController(ListarProdutosUseCase listarProdutosUseCase) {
        this.listarProdutosUseCase = listarProdutosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarProdutos() {
        List<Produto> produtos = listarProdutosUseCase.executar(null);
        List<ProdutoResponse> response = produtos.stream()
                .map(ProdutoResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProdutoResponse>> listarPorCategoria(@PathVariable Categoria categoria) {
        List<Produto> produtos = listarProdutosUseCase.executar(categoria);
        List<ProdutoResponse> response = produtos.stream()
                .map(ProdutoResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
