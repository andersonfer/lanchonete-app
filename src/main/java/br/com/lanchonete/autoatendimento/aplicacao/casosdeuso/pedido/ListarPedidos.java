package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import java.util.List;

public class ListarPedidos {

    private final PedidoGateway pedidoGateway;

    public ListarPedidos(final PedidoGateway pedidoGateway) {
        this.pedidoGateway = pedidoGateway;
    }

    public List<Pedido> executar() {
        return pedidoGateway.listarTodos();
    }
}