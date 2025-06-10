package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.controllers.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import java.util.Optional;

public class CadastrarCliente {

    private final ClienteGateway clienteGateway;

    public CadastrarCliente(final ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }


    public ClienteResponseDTO executar(final ClienteRequestDTO novoCliente) {

        try {

            validarDuplicidade(novoCliente);

            final Cliente cliente = Cliente.criar(
                    novoCliente.nome(),
                    novoCliente.email(),
                    novoCliente.cpf()
            );

            final Cliente clienteSalvo = clienteGateway.salvar(cliente);
            return ClienteResponseDTO.converterParaDTO(clienteSalvo);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }

    }

    private void validarDuplicidade(final ClienteRequestDTO novoCliente){
        final Optional<Cliente> clienteExistente = clienteGateway.buscarPorCpf(novoCliente.cpf());
        if (clienteExistente.isPresent()) {
            throw new ValidacaoException("CPF duplicado");
        }
    }
}
