package br.com.lanchonete.cozinha.adapters.web.controller;

import br.com.lanchonete.cozinha.adapters.messaging.publisher.PedidoProntoPublisher;
import br.com.lanchonete.cozinha.adapters.web.dto.PedidoCozinhaResponse;
import br.com.lanchonete.cozinha.application.usecases.IniciarPreparoPedidoUseCase;
import br.com.lanchonete.cozinha.application.usecases.ListarPedidosCozinhaUseCase;
import br.com.lanchonete.cozinha.application.usecases.MarcarPedidoComoProntoUseCase;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cozinha")
@Tag(name = "Cozinha", description = "Endpoints para gestão da fila da cozinha")
public class CozinhaController {

    private final ListarPedidosCozinhaUseCase listarPedidosCozinhaUseCase;
    private final IniciarPreparoPedidoUseCase iniciarPreparoPedidoUseCase;
    private final MarcarPedidoComoProntoUseCase marcarPedidoComoProntoUseCase;
    private final PedidoProntoPublisher pedidoProntoPublisher;

    public CozinhaController(ListarPedidosCozinhaUseCase listarPedidosCozinhaUseCase,
                             IniciarPreparoPedidoUseCase iniciarPreparoPedidoUseCase,
                             MarcarPedidoComoProntoUseCase marcarPedidoComoProntoUseCase,
                             PedidoProntoPublisher pedidoProntoPublisher) {
        this.listarPedidosCozinhaUseCase = listarPedidosCozinhaUseCase;
        this.iniciarPreparoPedidoUseCase = iniciarPreparoPedidoUseCase;
        this.marcarPedidoComoProntoUseCase = marcarPedidoComoProntoUseCase;
        this.pedidoProntoPublisher = pedidoProntoPublisher;
    }

    @Operation(
            summary = "Listar fila da cozinha",
            description = "Retorna a fila de pedidos da cozinha ordenada por prioridade e tempo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fila retornada com sucesso"
            )
    })
    @GetMapping("/fila")
    public ResponseEntity<List<PedidoCozinhaResponse>> listarFila() {
        List<PedidoCozinha> pedidos = listarPedidosCozinhaUseCase.executar();
        List<PedidoCozinhaResponse> response = pedidos.stream()
                .map(PedidoCozinhaResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Iniciar preparo de um pedido",
            description = "Marca um pedido como EM_PREPARO na fila da cozinha"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preparo iniciado com sucesso",
                    content = @Content(schema = @Schema(implementation = PedidoCozinhaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado na fila"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Pedido não está em status válido para iniciar preparo"
            )
    })
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<PedidoCozinhaResponse> iniciarPreparo(@PathVariable Long id) {
        PedidoCozinha pedido = iniciarPreparoPedidoUseCase.executar(id);
        return ResponseEntity.ok(PedidoCozinhaResponse.fromDomain(pedido));
    }

    @Operation(
            summary = "Marcar pedido como pronto",
            description = "Marca um pedido como PRONTO e publica evento PedidoPronto"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido marcado como pronto com sucesso",
                    content = @Content(schema = @Schema(implementation = PedidoCozinhaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado na fila"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Pedido não está em status válido para marcar como pronto"
            )
    })
    @PostMapping("/{id}/pronto")
    public ResponseEntity<PedidoCozinhaResponse> marcarComoPronto(@PathVariable Long id) {
        PedidoCozinha pedido = marcarPedidoComoProntoUseCase.executar(id);
        pedidoProntoPublisher.publicar(pedido.getPedidoId());
        return ResponseEntity.ok(PedidoCozinhaResponse.fromDomain(pedido));
    }
}
