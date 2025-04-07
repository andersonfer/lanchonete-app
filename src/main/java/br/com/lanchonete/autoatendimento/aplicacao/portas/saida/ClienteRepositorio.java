package br.com.lanchonete.autoatendimento.aplicacao.portas.saida;

import br.com.lanchonete.autoatendimento.dominio.Cliente;

import java.util.Optional;

public interface ClienteRepositorio {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
}
