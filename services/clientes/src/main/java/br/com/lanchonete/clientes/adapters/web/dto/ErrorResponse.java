package br.com.lanchonete.clientes.adapters.web.dto;

public record ErrorResponse(
        String mensagem,
        int status
) {
}
