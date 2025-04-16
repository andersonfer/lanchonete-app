package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.ListarPedidosService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.RealizarCheckoutService;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    public ListarPedidosService listarPedidosService(PedidoRepositorio pedidoRepositorio) {
        return new ListarPedidosService(pedidoRepositorio);
    }

    @Bean
    public RealizarCheckoutService realizarCheckoutService(PedidoRepositorio pedidoRepositorio,
                                                           ClienteRepositorio clienteRepositorio,
                                                           ProdutoRepositorio produtoRepositorio) {
        return new RealizarCheckoutService(pedidoRepositorio, clienteRepositorio, produtoRepositorio);
    }
}
