package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.adapters.web.dto.ProdutoResponse;
import br.com.lanchonete.pedidos.application.usecases.ListarProdutosUseCase;
import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Endpoints para consulta de produtos do cardápio")
public class ProdutoController {

    private final ListarProdutosUseCase listarProdutosUseCase;

    public ProdutoController(ListarProdutosUseCase listarProdutosUseCase) {
        this.listarProdutosUseCase = listarProdutosUseCase;
    }

    @Operation(
            summary = "Listar todos os produtos",
            description = "Retorna a lista completa de produtos disponíveis no cardápio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de produtos retornada com sucesso"
            )
    })
    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarProdutos() {
        List<Produto> produtos = listarProdutosUseCase.executar(null);
        List<ProdutoResponse> response = produtos.stream()
                .map(ProdutoResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar produtos por categoria",
            description = "Retorna os produtos filtrados por categoria específica (LANCHE, ACOMPANHAMENTO, BEBIDA, SOBREMESA)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de produtos da categoria retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Categoria inválida"
            )
    })
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProdutoResponse>> listarPorCategoria(@PathVariable Categoria categoria) {
        List<Produto> produtos = listarProdutosUseCase.executar(categoria);
        List<ProdutoResponse> response = produtos.stream()
                .map(ProdutoResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
