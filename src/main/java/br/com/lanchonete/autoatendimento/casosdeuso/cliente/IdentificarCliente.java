package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.shared.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import java.util.Optional;

public class IdentificarCliente {

    private final ClienteGateway clienteGateway;

    public IdentificarCliente(final ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public Optional<Cliente> executar(final String cpf) {

        if (cpf == null || cpf.isBlank()) {
            throw new ValidacaoException("CPF é obrigatório");
        }

        return clienteGateway.buscarPorCpf(cpf)
                .or(() -> {
                    throw new RecursoNaoEncontradoException("CPF não encontrado");
                });

    }

}
