package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteResponseDTO;

public interface CadastrarClienteCasoDeUso {
    ClienteResponseDTO executar(ClienteRequestDTO novoCliente);
}
