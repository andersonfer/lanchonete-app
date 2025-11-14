package br.com.lanchonete.pagamento.adapters.web.dto;

public record ErrorResponse(
        int status,
        String mensagem
) {
}
