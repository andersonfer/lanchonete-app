package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.AtualizarStatusPedido;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ConsultarStatusPagamento;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidosCozinha;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.webhook.ProcessarWebhookPagamento;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
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

    @Bean
    ConsultarStatusPagamento consultarStatusPagamento(final PedidoGateway pedidoGateway) {
        return new ConsultarStatusPagamento(pedidoGateway);
    }

    @Bean
    ProcessarWebhookPagamento processarWebhookPagamento(final PedidoGateway pedidoGateway) {
        return new ProcessarWebhookPagamento(pedidoGateway);
    }

    @Bean
    ListarPedidosCozinha listarPedidosCozinha(final PedidoGateway pedidoGateway) {
        return new ListarPedidosCozinha(pedidoGateway);
    }

    @Bean
    AtualizarStatusPedido atualizarStatusPedido(final PedidoGateway pedidoGateway) {
        return new AtualizarStatusPedido(pedidoGateway);
    }
}
