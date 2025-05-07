package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.adaptadores.persistencia.ClienteRepositorioJDBC;
import br.com.lanchonete.autoatendimento.adaptadores.persistencia.PedidoRepositorioJDBC;
import br.com.lanchonete.autoatendimento.adaptadores.persistencia.ProdutoRepositorioJDBC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RepositorioConfig {

    @Bean
    public ClienteRepositorio clienteRepositorio(JdbcTemplate jdbcTemplate) {
        return new ClienteRepositorioJDBC(jdbcTemplate);
    }

    @Bean
    public ProdutoRepositorio produtoRepositorio(JdbcTemplate jdbcTemplate) {
        return new ProdutoRepositorioJDBC(jdbcTemplate);
    }

    @Bean
    public PedidoRepositorio pedidoRepositorio(JdbcTemplate jdbcTemplate, ClienteRepositorio clienteRepositorio, ProdutoRepositorio produtoRepositorio) {
        return new PedidoRepositorioJDBC(jdbcTemplate, clienteRepositorio, produtoRepositorio);
    }
}