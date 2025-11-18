package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.adapters.web.dto.PedidoResponse;
import br.com.lanchonete.pedidos.adapters.web.dto.RealizarPedidoRequest;
import br.com.lanchonete.pedidos.application.usecases.BuscarPedidoUseCase;
import br.com.lanchonete.pedidos.application.usecases.CriarPedidoUseCase;
import br.com.lanchonete.pedidos.application.usecases.ListarPedidosUseCase;
import br.com.lanchonete.pedidos.application.usecases.RetirarPedidoUseCase;
import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para gestão de pedidos")
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final BuscarPedidoUseCase buscarPedidoUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final RetirarPedidoUseCase retirarPedidoUseCase;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase,
                            BuscarPedidoUseCase buscarPedidoUseCase,
                            ListarPedidosUseCase listarPedidosUseCase,
                            RetirarPedidoUseCase retirarPedidoUseCase) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.buscarPedidoUseCase = buscarPedidoUseCase;
        this.listarPedidosUseCase = listarPedidosUseCase;
        this.retirarPedidoUseCase = retirarPedidoUseCase;
    }

    @Operation(
            summary = "Realizar novo pedido",
            description = "Cria um novo pedido com os itens especificados. O pedido pode ser anônimo (cpfCliente null) ou identificado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou produtos não encontrados"
            )
    })
    @PostMapping
    public ResponseEntity<PedidoResponse> realizarPedido(@RequestBody RealizarPedidoRequest request) {
        log.info("CD Versionado SHA - Pedido solicitado");
        List<CriarPedidoUseCase.ItemPedidoInput> itensInput = request.getItens().stream()
                .map(item -> new CriarPedidoUseCase.ItemPedidoInput(item.getProdutoId(), item.getQuantidade()))
                .collect(Collectors.toList());

        Pedido pedido = criarPedidoUseCase.executar(request.getCpfCliente(), itensInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.fromDomain(pedido));
    }

    @Operation(
            summary = "Listar pedidos",
            description = "Lista todos os pedidos, opcionalmente filtrando por status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos retornada com sucesso"
            )
    })
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos(@RequestParam(required = false) StatusPedido status) {
        List<Pedido> pedidos = listarPedidosUseCase.executar(status);
        List<PedidoResponse> response = pedidos.stream()
                .map(PedidoResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna os dados completos de um pedido específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPedido(@PathVariable Long id) {
        Pedido pedido = buscarPedidoUseCase.executar(id);
        return ResponseEntity.ok(PedidoResponse.fromDomain(pedido));
    }

    @Operation(
            summary = "Retirar pedido",
            description = "Marca um pedido como retirado pelo cliente, alterando seu status para RETIRADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido marcado como retirado com sucesso",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Pedido não está em status válido para retirada"
            )
    })
    @PatchMapping("/{id}/retirar")
    public ResponseEntity<PedidoResponse> retirarPedido(@PathVariable Long id) {
        Pedido pedido = retirarPedidoUseCase.executar(id);
        return ResponseEntity.ok(PedidoResponse.fromDomain(pedido));
    }
}
