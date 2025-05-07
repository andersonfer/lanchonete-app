package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.implementacoes.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.implementacoes.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    public ListarPedidos listarPedidos(PedidoRepositorio pedidoRepositorio) {
        return new ListarPedidos(pedidoRepositorio);
    }

    @Bean
    public RealizarPedido realizarPedido(PedidoRepositorio pedidoRepositorio,
                                           ClienteRepositorio clienteRepositorio,
                                           ProdutoRepositorio produtoRepositorio) {
        return new RealizarPedido(pedidoRepositorio, clienteRepositorio, produtoRepositorio);
    }
}
