package br.com.lanchonete.clientes.application.gateways;

import br.com.lanchonete.clientes.domain.model.Cliente;

import java.util.Optional;

public interface ClienteGateway {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorId(Long id);
}
