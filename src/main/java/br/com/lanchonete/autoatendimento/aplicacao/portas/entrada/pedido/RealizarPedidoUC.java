package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;

public interface RealizarPedidoUC {
    PedidoResponseDTO executar(PedidoRequestDTO pedidoRequest);
}