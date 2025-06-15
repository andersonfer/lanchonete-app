package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
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
