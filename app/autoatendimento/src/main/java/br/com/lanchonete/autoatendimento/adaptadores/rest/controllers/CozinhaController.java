package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.AtualizarStatusPedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.EnumsMapper;
import br.com.lanchonete.autoatendimento.adaptadores.rest.servicos.CozinhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos/cozinha")
@Tag(name = "Cozinha", description = "API para gerenciamento de pedidos na cozinha")
public class CozinhaController {

    private final CozinhaService cozinhaService;
    private final EnumsMapper enumsMapper;

    public CozinhaController(final CozinhaService cozinhaService, final EnumsMapper enumsMapper) {
        this.cozinhaService = cozinhaService;
        this.enumsMapper = enumsMapper;
    }

    @GetMapping
    @Operation(
            summary = "Listar pedidos para cozinha",
            description = "Retorna pedidos com pagamento aprovado, ordenados por status e data",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
            }
    )
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosCozinha() {
        final List<PedidoResponseDTO> pedidos = cozinhaService.listarPedidosCozinha();
        return ResponseEntity.ok(pedidos);
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Atualizar status do pedido",
            description = "Atualiza o status de um pedido seguindo o fluxo: RECEBIDO → EM_PREPARACAO → PRONTO → FINALIZADO",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status do pedido atualizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
            }
    )
    public ResponseEntity<PedidoResponseDTO> atualizarStatusPedido(
            @PathVariable Long id,
            @RequestBody AtualizarStatusPedidoRequestDTO request) {
        final PedidoResponseDTO pedido = cozinhaService.atualizarStatusPedido(id, enumsMapper.statusPedidoParaDominio(request.status()));
        return ResponseEntity.ok(pedido);
    }
}