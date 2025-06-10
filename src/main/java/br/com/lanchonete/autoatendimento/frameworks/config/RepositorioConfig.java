package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.gateways.ClienteGatewayJDBC;
import br.com.lanchonete.autoatendimento.gateways.PedidoGatewayJDBC;
import br.com.lanchonete.autoatendimento.gateways.ProdutoGatewayJDBC;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
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