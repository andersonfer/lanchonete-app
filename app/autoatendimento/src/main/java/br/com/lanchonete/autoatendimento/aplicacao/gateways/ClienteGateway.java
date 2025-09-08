package br.com.lanchonete.autoatendimento.aplicacao.gateways;

import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;

import java.util.Optional;

public interface ClienteGateway {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorId(Long id);
}
