package br.com.lanchonete.autoatendimento.adaptadores.web.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final RealizarPedido realizarPedido;
    private final ListarPedidos listarPedidos;

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
    public ResponseEntity<PedidoResponseDTO> realizarCheckout(@RequestBody final PedidoRequestDTO pedidoRequest) {
        final PedidoResponseDTO pedidoResponse = realizarPedido.executar(pedidoRequest);
        return new ResponseEntity<>(pedidoResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Listar pedidos",
            description = "Retorna todos os pedidos cadastrados no sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
            }
    )
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos() {
        final List<PedidoResponseDTO> pedidos = listarPedidos.executar();
        return ResponseEntity.ok(pedidos);
    }
}