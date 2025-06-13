package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
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
            throw new ValidacaoException("CPF duplicado");
        }
    }
}
