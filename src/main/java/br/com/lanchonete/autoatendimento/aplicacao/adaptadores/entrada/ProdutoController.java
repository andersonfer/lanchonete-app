package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.BuscarProdutosPorCategoriaUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.CriarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.EditarProdutoUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.RemoverProdutoUC;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final CriarProdutoUC criarProdutoUC;
    private final EditarProdutoUC editarProdutoUC;
    private final RemoverProdutoUC removerProdutoUC;
    private final BuscarProdutosPorCategoriaUC buscarProdutosPorCategoriaUC;

    @PostMapping
    @Operation(
            summary = "Criar produto",
            description = "Cria um novo produto no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado")
            }
    )
    public ResponseEntity<ProdutoResponseDTO> criar(@RequestBody ProdutoRequestDTO produtoRequest) {
        ProdutoResponseDTO produto = criarProdutoUC.criar(produtoRequest);
        return new ResponseEntity<>(produto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Editar produto",
            description = "Atualiza os dados de um produto existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado"),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
            }
    )
    public ResponseEntity<ProdutoResponseDTO> editar(@PathVariable Long id, @RequestBody ProdutoRequestDTO produtoRequest) {
        ProdutoResponseDTO produto = editarProdutoUC.editar(id, produtoRequest);
        return ResponseEntity.ok(produto);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover produto",
            description = "Remove um produto do sistema",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
            }
    )
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        removerProdutoUC.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(
            summary = "Buscar produtos por categoria",
            description = "Retorna todos os produtos de uma determinada categoria",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Categoria inválida")
            }
    )
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(@PathVariable Categoria categoria) {
        List<ProdutoResponseDTO> produtos = buscarProdutosPorCategoriaUC.buscarProdutoPorCategoria(categoria);
        return ResponseEntity.ok(produtos);
    }
}