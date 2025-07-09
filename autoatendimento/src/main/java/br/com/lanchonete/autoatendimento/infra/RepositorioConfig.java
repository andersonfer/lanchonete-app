package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.adaptadores.persistencia.ClienteGatewayJDBC;
import br.com.lanchonete.autoatendimento.adaptadores.persistencia.PedidoGatewayJDBC;
import br.com.lanchonete.autoatendimento.adaptadores.persistencia.ProdutoGatewayJDBC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RepositorioConfig {

    @Bean
    ClienteGateway clienteRepositorio(final JdbcTemplate jdbcTemplate) {
        return new ClienteGatewayJDBC(jdbcTemplate);
    }

    @Bean
    ProdutoGateway produtoRepositorio(final JdbcTemplate jdbcTemplate) {
        return new ProdutoGatewayJDBC(jdbcTemplate);
    }

    @Bean
    PedidoGateway pedidoRepositorio(final JdbcTemplate jdbcTemplate, final ClienteGateway clienteGateway, final ProdutoGateway produtoGateway) {
        return new PedidoGatewayJDBC(jdbcTemplate, clienteGateway, produtoGateway);
    }
}