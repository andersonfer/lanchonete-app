package br.com.lanchonete.clientes.adapters.web.dto;

public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String cpf
) {
}
