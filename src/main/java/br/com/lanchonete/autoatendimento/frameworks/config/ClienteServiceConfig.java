package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.interfaces.ClienteRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClienteServiceConfig {

    @Bean
    CadastrarCliente cadastrarCliente(final ClienteRepositorio clienteRepositorio) {
        return new CadastrarCliente(clienteRepositorio);
    }

    @Bean
    IdentificarCliente identificarCliente(final ClienteRepositorio clienteRepositorio){
        return new IdentificarCliente(clienteRepositorio);
    }
}
