package br.com.lanchonete.autoatendimento.interfaces;

import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;

import java.util.Optional;

public interface ClienteRepositorio {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorId(Long id);
}
