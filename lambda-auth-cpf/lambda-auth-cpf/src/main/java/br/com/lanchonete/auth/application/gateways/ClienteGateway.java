package br.com.lanchonete.auth.application.gateways;

import br.com.lanchonete.auth.domain.entities.Cliente;

import java.util.Optional;

public interface ClienteGateway {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorId(Long id);
}