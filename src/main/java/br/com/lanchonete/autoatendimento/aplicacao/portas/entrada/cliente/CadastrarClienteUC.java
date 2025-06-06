package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.cliente;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ClienteResponseDTO;

public interface CadastrarClienteUC {
    ClienteResponseDTO executar(ClienteRequestDTO novoCliente);
}
