package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.api.ClienteApi;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.servicos.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ClienteController implements ClienteApi {

    private final ClienteService clienteService;

    public ClienteController(final ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(final ClienteRequestDTO novoCliente) {
        final ClienteResponseDTO clienteCadastrado = clienteService.cadastrarCliente(novoCliente);
        return new ResponseEntity<>(clienteCadastrado, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> identificarPorCpf(final String cpf) {
        final Optional<ClienteResponseDTO> clienteEncontrado = clienteService.identificarPorCpf(cpf);
        return clienteEncontrado
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
