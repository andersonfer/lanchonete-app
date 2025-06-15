package br.com.lanchonete.autoatendimento.aplicacao.servicos;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ItemPedidoInfo;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final RealizarPedido realizarPedido;
    private final ListarPedidos listarPedidos;

    public PedidoService(final RealizarPedido realizarPedido,
                         final ListarPedidos listarPedidos) {
        this.realizarPedido = realizarPedido;
        this.listarPedidos = listarPedidos;
    }

    @Transactional
    public PedidoResponseDTO realizarCheckout(final PedidoRequestDTO pedidoRequest) {
        // Converter DTOs para a estrutura interna do UC
        List<ItemPedidoInfo> itensInfo = pedidoRequest.itens().stream()
                .map(item -> new ItemPedidoInfo(
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