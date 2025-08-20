package br.com.lanchonete.auth.application.usecases;

import br.com.lanchonete.auth.domain.entities.Cliente;
import br.com.lanchonete.auth.domain.exceptions.RecursoNaoEncontradoException;
import br.com.lanchonete.auth.domain.valueobjects.Cpf;
import br.com.lanchonete.auth.application.gateways.ClienteGateway;

public class IdentificarCliente {

    private final ClienteGateway clienteGateway;

    public IdentificarCliente(final ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public Cliente executar(final String cpf) {
        // CPF vazio/null → cliente anônimo (REGRA DE NEGÓCIO)
        if (cpf == null || cpf.trim().isEmpty()) {
            return Cliente.criarAnonimo();
        }
        
        // CPF informado → validar formato e buscar
        Cpf cpfValido = new Cpf(cpf); // Valida formato 11 dígitos
        return clienteGateway.buscarPorCpf(cpfValido.getValor())
                .orElseThrow(() -> new RecursoNaoEncontradoException("CPF não encontrado"));
    }

}