package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    ListarPedidos listarPedidos(final PedidoGateway pedidoGateway) {
        return new ListarPedidos(pedidoGateway);
    }

    @Bean
    RealizarPedido realizarPedido(final PedidoGateway pedidoGateway,
                                  final ClienteGateway clienteGateway,
                                  final ProdutoGateway produtoGateway) {
        return new RealizarPedido(pedidoGateway, clienteGateway, produtoGateway);
    }
}
