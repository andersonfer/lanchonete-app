package br.com.lanchonete.clientes.adapters.web.dto;

public record ClienteRequest(
        String nome,
        String email,
        String cpf
) {
}
