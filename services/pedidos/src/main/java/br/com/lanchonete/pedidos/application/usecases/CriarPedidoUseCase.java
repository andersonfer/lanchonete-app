package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.ClienteGateway;
import br.com.lanchonete.pedidos.application.gateways.EventPublisher;
import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.application.gateways.ProdutoGateway;
import br.com.lanchonete.pedidos.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.exceptions.ProdutoNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.model.ItemPedido;
import br.com.lanchonete.pedidos.domain.model.Pedido;
import br.com.lanchonete.pedidos.domain.model.Produto;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;

import java.time.LocalDateTime;
import java.util.List;

public class CriarPedidoUseCase {

    private final PedidoGateway pedidoGateway;
    private final ProdutoGateway produtoGateway;
    private final ClienteGateway clienteGateway;
    private final EventPublisher eventPublisher;

    public CriarPedidoUseCase(PedidoGateway pedidoGateway,
                              ProdutoGateway produtoGateway,
                              ClienteGateway clienteGateway,
                              EventPublisher eventPublisher) {
        this.pedidoGateway = pedidoGateway;
        this.produtoGateway = produtoGateway;
        this.clienteGateway = clienteGateway;
        this.eventPublisher = eventPublisher;
    }

    public Pedido executar(String cpfCliente, List<ItemPedidoInput> itensInput) {
        // Validar cliente se CPF foi informado
        if (cpfCliente != null && !cpfCliente.isBlank()) {
            if (!clienteGateway.existeCliente(cpfCliente)) {
                throw new ClienteNaoEncontradoException(cpfCliente);
            }
        }

        // Criar pedido
        Pedido pedido = Pedido.criar(cpfCliente, StatusPedido.CRIADO, LocalDateTime.now());

        // Buscar nome do cliente se CPF foi informado
        if (cpfCliente != null && !cpfCliente.isBlank()) {
            String nomeCliente = clienteGateway.buscarNomeCliente(cpfCliente);
            pedido.setClienteNome(nomeCliente);
        }

        // Adicionar itens ao pedido
        for (ItemPedidoInput itemInput : itensInput) {
            Produto produto = produtoGateway.buscarPorId(itemInput.getProdutoId())
                    .orElseThrow(() -> new ProdutoNaoEncontradoException(itemInput.getProdutoId()));

            ItemPedido item = ItemPedido.criar(produto, itemInput.getQuantidade());
            pedido.adicionarItem(item);
        }

        // Validar pedido
        pedido.validar();

        // Salvar pedido
        Pedido pedidoSalvo = pedidoGateway.salvar(pedido);

        // Publicar evento PedidoCriado
        eventPublisher.publicarPedidoCriado(
                pedidoSalvo.getId(),
                pedidoSalvo.getValorTotal().toString(),
                pedidoSalvo.getCpfCliente()
        );

        return pedidoSalvo;
    }

    public static class ItemPedidoInput {
        private Long produtoId;
        private int quantidade;

        public ItemPedidoInput() {}

        public ItemPedidoInput(Long produtoId, int quantidade) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
        }

        public Long getProdutoId() {
            return produtoId;
        }

        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(int quantidade) {
            this.quantidade = quantidade;
        }
    }
}
