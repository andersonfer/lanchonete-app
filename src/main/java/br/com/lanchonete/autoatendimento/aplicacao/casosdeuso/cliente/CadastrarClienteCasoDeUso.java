package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;

public interface CadastrarClienteCasoDeUso {
    ClienteResponseDTO executar(ClienteRequestDTO novoCliente);
}
