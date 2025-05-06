package br.com.lanchonete.autoatendimento.aplicacao.repositorios;

import br.com.lanchonete.autoatendimento.aplicacao.dominio.Cliente;

import java.util.Optional;

public interface ClienteRepositorio {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
    Optional<Cliente> buscarPorId(Long id);
}
