package br.com.lanchonete.autoatendimento.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
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