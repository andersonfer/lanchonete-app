package br.com.lanchonete.autoatendimento.adaptadores.rest.servicos;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ConsultarStatusPagamento;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ItemPedidoInfo;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPagamentoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.PedidoMapper;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final RealizarPedido realizarPedido;
    private final ListarPedidos listarPedidos;
    private final ConsultarStatusPagamento consultarStatusPagamento;
    private final PedidoMapper pedidoMapper;

    public PedidoService(final RealizarPedido realizarPedido,
                         final ListarPedidos listarPedidos,
                         final ConsultarStatusPagamento consultarStatusPagamento,
                         final PedidoMapper pedidoMapper) {
        this.realizarPedido = realizarPedido;
        this.listarPedidos = listarPedidos;
        this.consultarStatusPagamento = consultarStatusPagamento;
        this.pedidoMapper = pedidoMapper;
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
        return pedidoMapper.paraDTO(pedidoSalvo);
    }

    public List<PedidoResponseDTO> listarPedidos() {
        List<Pedido> pedidos = listarPedidos.executar();
        return pedidos.stream()
                .map(pedidoMapper::paraDTO)
                .toList();
    }

    public StatusPagamentoResponseDTO consultarStatusPagamento(Long pedidoId) {
        StatusPagamento statusPagamento = consultarStatusPagamento.executar(pedidoId);
        return StatusPagamentoResponseDTO.de(pedidoId, statusPagamento);
    }
}