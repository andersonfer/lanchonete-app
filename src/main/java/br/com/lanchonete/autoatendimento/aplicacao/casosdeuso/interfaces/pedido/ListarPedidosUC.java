package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;

import java.util.List;

public interface ListarPedidosUC {
    List<PedidoResponseDTO> executar();
}