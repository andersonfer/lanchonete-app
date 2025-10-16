package br.com.lanchonete.clientes.infrastructure.config;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.application.usecases.BuscarClientePorCpf;
import br.com.lanchonete.clientes.application.usecases.CadastrarCliente;
import br.com.lanchonete.clientes.application.usecases.IdentificarCliente;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    CadastrarCliente cadastrarCliente(final ClienteGateway clienteGateway) {
        return new CadastrarCliente(clienteGateway);
    }

    @Bean
    IdentificarCliente identificarCliente(final ClienteGateway clienteGateway) {
        return new IdentificarCliente(clienteGateway);
    }

    @Bean
    BuscarClientePorCpf buscarClientePorCpf(final ClienteGateway clienteGateway) {
        return new BuscarClientePorCpf(clienteGateway);
    }
}
