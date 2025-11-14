package br.com.lanchonete.clientes.application.usecases;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.clientes.domain.model.Cliente;

public class BuscarClientePorCpf {

    private final ClienteGateway clienteGateway;

    public BuscarClientePorCpf(final ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public Cliente executar(final String cpf) {
        return clienteGateway.buscarPorCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
    }
}
