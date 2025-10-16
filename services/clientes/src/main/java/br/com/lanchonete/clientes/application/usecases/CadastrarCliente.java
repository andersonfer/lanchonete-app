package br.com.lanchonete.clientes.application.usecases;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.exceptions.ValidacaoException;
import br.com.lanchonete.clientes.domain.model.Cliente;

import java.util.Optional;

public class CadastrarCliente {

    private final ClienteGateway clienteGateway;

    public CadastrarCliente(final ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public Cliente executar(final String nome, final String email, final String cpf) {
        try {
            validarDuplicidade(cpf);
            final Cliente cliente = Cliente.criar(nome, email, cpf);
            return clienteGateway.salvar(cliente);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }
    }

    private void validarDuplicidade(final String cpf){
        final Optional<Cliente> clienteExistente = clienteGateway.buscarPorCpf(cpf);
        if (clienteExistente.isPresent()) {
            throw new ValidacaoException("CPF já cadastrado");
        }
    }
}
