package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoAdaptador {

    private final RealizarPedido realizarPedido;
    private final ListarPedidos listarPedidos;

    @Transactional
    public PedidoResponseDTO realizarCheckout(final PedidoRequestDTO pedidoRequest) {
        return realizarPedido.executar(pedidoRequest);
    }

    public List<PedidoResponseDTO> listarPedidos() {
        return listarPedidos.executar();
    }
}