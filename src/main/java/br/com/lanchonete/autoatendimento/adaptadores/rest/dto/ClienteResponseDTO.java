package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;

public record ClienteResponseDTO(Long id, String nome, String cpf, String email) {

    public static ClienteResponseDTO converterParaDTO(Cliente cliente) {

        return (cliente != null)
                ? new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf().getValor(),
                cliente.getEmail().getValor())
                : null;
    }
}