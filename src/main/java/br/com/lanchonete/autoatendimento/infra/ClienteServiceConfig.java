package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.repositorios.ClienteRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClienteServiceConfig {

    @Bean
    public CadastrarCliente cadastrarCliente(ClienteRepositorio clienteRepositorio) {
        return new CadastrarCliente(clienteRepositorio);
    }

    @Bean
    public IdentificarCliente identificarCliente(ClienteRepositorio clienteRepositorio){
        return new IdentificarCliente(clienteRepositorio);
    }
}
