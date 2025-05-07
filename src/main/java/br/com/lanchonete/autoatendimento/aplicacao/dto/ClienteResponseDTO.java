package br.com.lanchonete.autoatendimento.aplicacao.dto;

import br.com.lanchonete.autoatendimento.dominio.modelo.Cliente;

public record ClienteResponseDTO(Long id, String nome, String cpf, String email) {

    public static ClienteResponseDTO converterParaDTO(Cliente cliente) {

        return (cliente != null)
                ? new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail())
                : null;
    }
}