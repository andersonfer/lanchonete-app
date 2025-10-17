package br.com.lanchonete.pedidos.adapters.web;

import br.com.lanchonete.pedidos.adapters.web.dto.PedidoResponse;
import br.com.lanchonete.pedidos.adapters.web.dto.RealizarPedidoRequest;
import br.com.lanchonete.pedidos.application.usecases.BuscarPedidoUseCase;
import br.com.lanchonete.pedidos.application.usecases.CriarPedidoUseCase;
import br.com.lanchonete.pedidos.application.usecases.ListarPedidosUseCase;
import br.com.lanchonete.pedidos.application.usecases.RetirarPedidoUseCase;
import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

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

    @PostMapping
    public ResponseEntity<PedidoResponse> realizarPedido(@RequestBody RealizarPedidoRequest request) {
        List<CriarPedidoUseCase.ItemPedidoInput> itensInput = request.getItens().stream()
                .map(item -> new CriarPedidoUseCase.ItemPedidoInput(item.getProdutoId(), item.getQuantidade()))
                .collect(Collectors.toList());

        Pedido pedido = criarPedidoUseCase.executar(request.getCpfCliente(), itensInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.fromDomain(pedido));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos(@RequestParam(required = false) StatusPedido status) {
        List<Pedido> pedidos = listarPedidosUseCase.executar(status);
        List<PedidoResponse> response = pedidos.stream()
                .map(PedidoResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPedido(@PathVariable Long id) {
        Pedido pedido = buscarPedidoUseCase.executar(id);
        return ResponseEntity.ok(PedidoResponse.fromDomain(pedido));
    }

    @PatchMapping("/{id}/retirar")
    public ResponseEntity<PedidoResponse> retirarPedido(@PathVariable Long id) {
        Pedido pedido = retirarPedidoUseCase.executar(id);
        return ResponseEntity.ok(PedidoResponse.fromDomain(pedido));
    }
}
