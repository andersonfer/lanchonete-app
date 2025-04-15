package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;

public interface CadastrarClienteUC {
    ClienteResponseDTO cadastrar(ClienteRequestDTO novoCliente);
}
