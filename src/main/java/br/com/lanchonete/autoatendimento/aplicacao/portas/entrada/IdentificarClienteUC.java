package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;

import java.util.Optional;

public interface IdentificarClienteUC {
    Optional<ClienteResponseDTO> identificar(String cpf);
}
