package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ClienteGateway;
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
