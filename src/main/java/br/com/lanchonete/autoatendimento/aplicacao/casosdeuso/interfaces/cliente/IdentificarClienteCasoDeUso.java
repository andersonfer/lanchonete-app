package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;

import java.util.Optional;

public interface IdentificarClienteCasoDeUso {
    Optional<ClienteResponseDTO> executar(String cpf);
}
