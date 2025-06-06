package br.com.lanchonete.autoatendimento.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import br.com.lanchonete.autoatendimento.interfaces.PedidoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPedidos {

    private final PedidoRepositorio pedidoRepositorio;

    public List<PedidoResponseDTO> executar() {
        final List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        return pedidos.stream()
                .map(PedidoResponseDTO::converterParaDTO)
                .toList();
    }
}