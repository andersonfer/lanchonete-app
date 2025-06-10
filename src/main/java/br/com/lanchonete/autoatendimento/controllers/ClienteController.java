package br.com.lanchonete.autoatendimento.controllers;

import br.com.lanchonete.autoatendimento.api.ClienteApi;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.ClienteAdaptador;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClienteController implements ClienteApi {

    private final ClienteAdaptador clienteAdaptador;

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
