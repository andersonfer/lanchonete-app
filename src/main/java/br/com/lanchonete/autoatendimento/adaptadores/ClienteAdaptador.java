package br.com.lanchonete.autoatendimento.adaptadores;

import br.com.lanchonete.autoatendimento.controllers.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteAdaptador {

    private final CadastrarCliente cadastrarCliente;
    private final IdentificarCliente identificarCliente;

    @Transactional
    public ClienteResponseDTO cadastrarCliente(final ClienteRequestDTO novoCliente) {
        Cliente clienteSalvo = cadastrarCliente.executar(
                novoCliente.nome(),
                novoCliente.email(),
                novoCliente.cpf()
        );
        return ClienteResponseDTO.converterParaDTO(clienteSalvo);
    }

    public Optional<ClienteResponseDTO> identificarPorCpf(final String cpf) {
        return identificarCliente.executar(cpf)
                .map(ClienteResponseDTO::converterParaDTO);
    }
}