package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.CadastrarClienteService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.IdentificarClienteService;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClienteServiceConfig {

    @Bean
    public CadastrarClienteService cadastrarClienteService(ClienteRepositorio clienteRepositorio) {
        return new CadastrarClienteService(clienteRepositorio);
    }

    @Bean
    public IdentificarClienteService identificarClienteService(ClienteRepositorio clienteRepositorio){
        return new IdentificarClienteService(clienteRepositorio);
    }
}
