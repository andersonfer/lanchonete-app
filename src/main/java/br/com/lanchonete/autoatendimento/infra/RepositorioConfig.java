package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.saida.ClienteRepositorioJDBC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RepositorioConfig {

    @Bean
    public ClienteRepositorio clienteRepositorio(JdbcTemplate jdbcTemplate) {
        return new ClienteRepositorioJDBC(jdbcTemplate);
    }

}
