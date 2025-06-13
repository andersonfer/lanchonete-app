package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoAdaptador {

    private final RealizarPedido realizarPedido;
    private final ListarPedidos listarPedidos;

    public PedidoAdaptador(final RealizarPedido realizarPedido,
                          final ListarPedidos listarPedidos) {
        this.realizarPedido = realizarPedido;
        this.listarPedidos = listarPedidos;
    }

    @Transactional
    public PedidoResponseDTO realizarCheckout(final PedidoRequestDTO pedidoRequest) {
        // Converter DTOs para a estrutura interna do UC
        List<br.com.lanchonete.autoatendimento.casosdeuso.pedido.ItemPedidoInfo> itensInfo = pedidoRequest.itens().stream()
                .map(item -> new br.com.lanchonete.autoatendimento.casosdeuso.pedido.ItemPedidoInfo(
                        item.produtoId(), 
                        item.quantidade()))
                .toList();
        
        Pedido pedidoSalvo = realizarPedido.executar(pedidoRequest.cpfCliente(), itensInfo);
        return PedidoResponseDTO.converterParaDTO(pedidoSalvo);
    }

    public List<PedidoResponseDTO> listarPedidos() {
        List<Pedido> pedidos = listarPedidos.executar();
        return pedidos.stream()
                .map(PedidoResponseDTO::converterParaDTO)
                .toList();
    }
}