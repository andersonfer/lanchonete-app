package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;

import java.util.List;

public interface ListarPedidosUC {
    List<PedidoResponseDTO> executar();
}