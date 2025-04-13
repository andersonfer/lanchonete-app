package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.Cliente;

public class ClienteMapper {

    private ClienteMapper() {};

    public static ClienteResponseDTO converterParaResponseDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .cpf(cliente.getCpf())
                .build();
    }
}
