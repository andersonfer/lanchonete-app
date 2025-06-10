package br.com.lanchonete.autoatendimento.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.controllers.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import br.com.lanchonete.autoatendimento.entidades.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public class RealizarPedido {

    private final PedidoGateway pedidoGateway;
    private final ClienteGateway clienteGateway;
    private final ProdutoGateway produtoGateway;

    public RealizarPedido(final PedidoGateway pedidoGateway,
                          final ClienteGateway clienteGateway,
                          final ProdutoGateway produtoGateway) {
        this.pedidoGateway = pedidoGateway;
        this.clienteGateway = clienteGateway;
        this.produtoGateway = produtoGateway;
    }


    public PedidoResponseDTO executar(final PedidoRequestDTO novoPedido) {

        if (novoPedido == null) {
            throw new ValidacaoException("Pedido não pode ser nulo");
        }

        try {
            final Cliente cliente = buscarCliente(novoPedido.cpfCliente());

            final Pedido pedido = Pedido.criar(
                    cliente,
                    StatusPedido.RECEBIDO,
                    LocalDateTime.now()
            );

            adicionarItensAoPedido(pedido,novoPedido.itens());

            pedido.validar();

            final Pedido pedidoSalvo = pedidoGateway.salvar(pedido);

            return PedidoResponseDTO.converterParaDTO(pedidoSalvo);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }

    }

    private void adicionarItensAoPedido(final Pedido pedido, final List<ItemPedidoDTO> itens) {

        for (final ItemPedidoDTO itemDTO : itens) {
            final Produto produto = produtoGateway.buscarPorId(itemDTO.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + itemDTO.produtoId()));

            final ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemDTO.quantidade())
                    .valorUnitario(produto.getPreco())
                    .build();

            item.calcularValorTotal();
            pedido.adicionarItem(item);
        }
    }

    private Cliente buscarCliente(final String cpf) {
       if (StringUtils.isNotBlank(cpf)) {
           return clienteGateway.buscarPorCpf(cpf)
                   .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o CPF informado"));
       } else {
           return null;
       }
   }

}