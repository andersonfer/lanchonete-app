package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    ListarPedidos listarPedidos(final PedidoRepositorio pedidoRepositorio) {
        return new ListarPedidos(pedidoRepositorio);
    }

    @Bean
    RealizarPedido realizarPedido(final PedidoRepositorio pedidoRepositorio,
                                  final ClienteRepositorio clienteRepositorio,
                                  final ProdutoRepositorio produtoRepositorio) {
        return new RealizarPedido(pedidoRepositorio, clienteRepositorio, produtoRepositorio);
    }
}
