package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.api.ClienteApi;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.servicos.ClienteAdaptador;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClienteController implements ClienteApi {

    private final ClienteAdaptador clienteAdaptador;

    public ClienteController(final ClienteAdaptador clienteAdaptador) {
        this.clienteAdaptador = clienteAdaptador;
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(final ClienteRequestDTO novoCliente) {
        final ClienteResponseDTO clienteCadastrado = clienteAdaptador.cadastrarCliente(novoCliente);
        return new ResponseEntity<>(clienteCadastrado, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> identificarPorCpf(final String cpf) {
        final Optional<ClienteResponseDTO> clienteEncontrado = clienteAdaptador.identificarPorCpf(cpf);
        return clienteEncontrado
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
