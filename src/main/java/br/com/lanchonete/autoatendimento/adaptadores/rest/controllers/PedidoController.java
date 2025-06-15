package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.api.PedidoApi;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.servicos.PedidoAdaptador;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PedidoController implements PedidoApi {

    private final PedidoAdaptador pedidoAdaptador;

    public PedidoController(final PedidoAdaptador pedidoAdaptador) {
        this.pedidoAdaptador = pedidoAdaptador;
    }

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