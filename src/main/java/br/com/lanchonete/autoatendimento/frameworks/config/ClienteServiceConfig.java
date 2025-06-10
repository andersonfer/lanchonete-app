package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClienteServiceConfig {

    @Bean
    CadastrarCliente cadastrarCliente(final ClienteGateway clienteGateway) {
        return new CadastrarCliente(clienteGateway);
    }

    @Bean
    IdentificarCliente identificarCliente(final ClienteGateway clienteGateway){
        return new IdentificarCliente(clienteGateway);
    }
}
