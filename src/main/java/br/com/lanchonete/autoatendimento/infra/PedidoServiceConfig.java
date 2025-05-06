package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarCheckout;
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
    public RealizarCheckout realizarCheckout(PedidoRepositorio pedidoRepositorio,
                                                    ClienteRepositorio clienteRepositorio,
                                                    ProdutoRepositorio produtoRepositorio) {
        return new RealizarCheckout(pedidoRepositorio, clienteRepositorio, produtoRepositorio);
    }
}
