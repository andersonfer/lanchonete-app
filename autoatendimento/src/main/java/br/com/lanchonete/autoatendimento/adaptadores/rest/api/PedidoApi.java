package br.com.lanchonete.autoatendimento.adaptadores.rest.api;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPagamentoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
public interface PedidoApi {

    @PostMapping("/checkout")
    @Operation(
            summary = "Realizar checkout de pedido",
            description = "Cria um novo pedido no sistema e o envia para a fila de preparação",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Cliente ou produto não encontrado")
            }
    )
    ResponseEntity<PedidoResponseDTO> realizarCheckout(@RequestBody PedidoRequestDTO pedidoRequest);

    @GetMapping
    @Operation(
            summary = "Listar pedidos",
            description = "Retorna todos os pedidos cadastrados no sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
            }
    )
    ResponseEntity<List<PedidoResponseDTO>> listarPedidos();

    @GetMapping("/{id}/pagamento/status")
    @Operation(
            summary = "Consultar status de pagamento",
            description = "Retorna o status atual do pagamento do pedido",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status de pagamento retornado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
            }
    )
    ResponseEntity<StatusPagamentoResponseDTO> consultarStatusPagamento(@PathVariable Long id);
}