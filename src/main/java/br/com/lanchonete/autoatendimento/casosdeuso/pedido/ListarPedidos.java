package br.com.lanchonete.autoatendimento.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.pedido.ListarPedidosUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPedidos implements ListarPedidosUC {

    private final PedidoRepositorio pedidoRepositorio;

    @Override
    public List<PedidoResponseDTO> executar() {
        final List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        return pedidos.stream()
                .map(PedidoResponseDTO::converterParaDTO)
                .toList();
    }
}