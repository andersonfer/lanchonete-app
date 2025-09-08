package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;

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


    public Pedido executar(final String cpfCliente, final List<ItemPedidoInfo> itens) {

        if (itens == null || itens.isEmpty()) {
            throw new ValidacaoException("Pedido deve conter pelo menos um item");
        }

        try {
            final Cliente cliente = buscarCliente(cpfCliente);

            final Pedido pedido = Pedido.criar(
                    cliente,
                    StatusPedido.RECEBIDO,
                    LocalDateTime.now()
            );

            adicionarItensAoPedido(pedido, itens);

            pedido.validar();

            return pedidoGateway.salvar(pedido);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }

    }

    private void adicionarItensAoPedido(final Pedido pedido, final List<ItemPedidoInfo> itens) {

        for (final ItemPedidoInfo itemInfo : itens) {
            final Produto produto = produtoGateway.buscarPorId(itemInfo.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + itemInfo.produtoId()));

            final ItemPedido item = ItemPedido.criar(
                    produto,
                    itemInfo.quantidade()
            );

            pedido.adicionarItem(item);
        }
    }

    private Cliente buscarCliente(final String cpf) {
       if (cpf != null && !cpf.trim().isEmpty()) {
           return clienteGateway.buscarPorCpf(cpf)
                   .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o CPF informado"));
       } else {
           return null;
       }
   }

}