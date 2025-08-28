package br.com.lanchonete.auth.application.gateways;

import br.com.lanchonete.auth.domain.entities.Cliente;

import java.util.Optional;

public interface ClienteGateway {
    Optional<Cliente> buscarPorCpf(String cpf);
}