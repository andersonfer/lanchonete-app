package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoResponseDTO;

import java.util.List;

public interface ListarPedidosUC {
    List<PedidoResponseDTO> listarTodos();
}