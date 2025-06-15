package br.com.lanchonete.autoatendimento.aplicacao.servicos;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClienteService {

    private final CadastrarCliente cadastrarCliente;
    private final IdentificarCliente identificarCliente;

    public ClienteService(final CadastrarCliente cadastrarCliente,
                          final IdentificarCliente identificarCliente) {
        this.cadastrarCliente = cadastrarCliente;
        this.identificarCliente = identificarCliente;
    }

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