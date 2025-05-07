package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteResponseDTO;

import java.util.Optional;

public interface IdentificarClienteUC {
    Optional<ClienteResponseDTO> executar(String cpf);
}
