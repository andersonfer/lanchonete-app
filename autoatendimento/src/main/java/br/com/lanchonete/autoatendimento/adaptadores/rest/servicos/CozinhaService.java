package br.com.lanchonete.autoatendimento.adaptadores.rest.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.mappers.PedidoMapper;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.AtualizarStatusPedido;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidosCozinha;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CozinhaService {

    private final ListarPedidosCozinha listarPedidosCozinha;
    private final AtualizarStatusPedido atualizarStatusPedido;
    private final PedidoGateway pedidoGateway;
    private final PedidoMapper pedidoMapper;

    public CozinhaService(final ListarPedidosCozinha listarPedidosCozinha,
                          final AtualizarStatusPedido atualizarStatusPedido,
                          final PedidoGateway pedidoGateway,
                          final PedidoMapper pedidoMapper) {
        this.listarPedidosCozinha = listarPedidosCozinha;
        this.atualizarStatusPedido = atualizarStatusPedido;
        this.pedidoGateway = pedidoGateway;
        this.pedidoMapper = pedidoMapper;
    }

    public List<PedidoResponseDTO> listarPedidosCozinha() {
        List<Pedido> pedidos = listarPedidosCozinha.executar();
        return pedidos.stream()
                .map(pedidoMapper::paraDTO)
                .toList();
    }

    @Transactional
    public PedidoResponseDTO atualizarStatusPedido(final Long pedidoId, final StatusPedido novoStatus) {
        // Atualizar o status
        atualizarStatusPedido.executar(pedidoId, novoStatus);
        
        // Buscar o pedido atualizado para retornar
        Pedido pedidoAtualizado = pedidoGateway.buscarPorId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado após atualização"));
        
        return pedidoMapper.paraDTO(pedidoAtualizado);
    }
}