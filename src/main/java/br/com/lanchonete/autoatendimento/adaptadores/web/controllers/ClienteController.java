package br.com.lanchonete.autoatendimento.adaptadores.web.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.web.api.ClienteApi;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.IdentificarCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClienteController implements ClienteApi {

    private final CadastrarCliente cadastrarCliente;
    private final IdentificarCliente identificarCliente;

    @Override
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(final ClienteRequestDTO novoCliente) {
        final ClienteResponseDTO clienteCadastrado = cadastrarCliente.executar(novoCliente);
        return new ResponseEntity<>(clienteCadastrado, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ClienteResponseDTO> identificarPorCpf(final String cpf) {
        final Optional<ClienteResponseDTO> clienteEncontrado = identificarCliente.executar(cpf);
        return clienteEncontrado
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
