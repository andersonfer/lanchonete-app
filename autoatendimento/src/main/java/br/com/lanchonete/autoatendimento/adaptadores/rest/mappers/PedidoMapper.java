package br.com.lanchonete.autoatendimento.adaptadores.rest.mappers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ItemPedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PedidoMapper implements Mapper<Pedido, PedidoResponseDTO> {

    private final EnumsMapper enumsMapper;

    public PedidoMapper(EnumsMapper enumsMapper) {
        this.enumsMapper = enumsMapper;
    }

    @Override
    public PedidoResponseDTO paraDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        Long clienteId = null;
        String nomeCliente = null;

        if (pedido.getCliente() != null) {
            clienteId = pedido.getCliente().getId();
            nomeCliente = pedido.getCliente().getNome();
        }

        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens().stream()
                .map(ItemPedidoResponseDTO::converterParaDTO)
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getNumeroPedido() != null ? pedido.getNumeroPedido().getValor() : null,
                clienteId,
                nomeCliente,
                itensDTO,
                enumsMapper.statusPedidoParaDTO(pedido.getStatus()),
                enumsMapper.statusPagamentoParaDTO(pedido.getStatusPagamento()),
                pedido.getDataCriacao(),
                pedido.getValorTotal()
        );
    }

    @Override
    public Pedido paraDominio(PedidoResponseDTO dto) {
        throw new UnsupportedOperationException("Conversão de PedidoResponseDTO para Pedido não é suportada");
    }
}