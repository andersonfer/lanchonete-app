package br.com.lanchonete.autoatendimento.adaptadores.rest.api;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
public interface ProdutoApi {

    @PostMapping
    @Operation(
            summary = "Criar produto",
            description = "Cria um novo produto no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado")
            }
    )
    ResponseEntity<ProdutoResponseDTO> criar(@RequestBody ProdutoRequestDTO produtoRequest);

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
    ResponseEntity<ProdutoResponseDTO> editar(@PathVariable Long id, @RequestBody ProdutoRequestDTO produtoRequest);

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover produto",
            description = "Remove um produto do sistema",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
            }
    )
    ResponseEntity<Void> remover(@PathVariable Long id);

    @GetMapping("/categoria/{categoria}")
    @Operation(
            summary = "Buscar produtos por categoria",
            description = "Retorna todos os produtos de uma determinada categoria",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Categoria inválida")
            }
    )
    ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(@PathVariable Categoria categoria);
}