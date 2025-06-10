package br.com.lanchonete.autoatendimento.controllers;

import br.com.lanchonete.autoatendimento.api.PedidoApi;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.PedidoAdaptador;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PedidoController implements PedidoApi {

    private final PedidoAdaptador pedidoAdaptador;

    @Override
    public ResponseEntity<PedidoResponseDTO> realizarCheckout(final PedidoRequestDTO pedidoRequest) {
        final PedidoResponseDTO pedidoResponse = pedidoAdaptador.realizarCheckout(pedidoRequest);
        return new ResponseEntity<>(pedidoResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos() {
        final List<PedidoResponseDTO> pedidos = pedidoAdaptador.listarPedidos();
        return ResponseEntity.ok(pedidos);
    }
}