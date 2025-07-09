package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import java.util.List;
import java.util.stream.Collectors;

public class ListarPedidosCozinha {

    private final PedidoGateway pedidoGateway;

    public ListarPedidosCozinha(final PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public List<Pedido> executar() {
        return pedidoGateway.listarTodos()
                .stream()
                .filter(pedido -> pedido.getStatusPagamento() == StatusPagamento.APROVADO)
                .collect(Collectors.toList());
    }
}