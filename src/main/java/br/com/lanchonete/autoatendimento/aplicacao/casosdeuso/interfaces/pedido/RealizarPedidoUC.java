package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoResponseDTO;

public interface RealizarPedidoUC {
    PedidoResponseDTO executar(PedidoRequestDTO pedidoRequest);
}