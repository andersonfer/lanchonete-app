package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.cliente.CadastrarClienteUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CadastrarCliente implements CadastrarClienteUC {

    private final ClienteRepositorio clienteRepositorio;


    @Override
    public ClienteResponseDTO executar(final ClienteRequestDTO novoCliente) {

        try {

            validarDuplicidade(novoCliente);

            final Cliente cliente = Cliente.criar(
                    novoCliente.nome(),
                    novoCliente.email(),
                    novoCliente.cpf()
            );

            final Cliente clienteSalvo = clienteRepositorio.salvar(cliente);
            return ClienteResponseDTO.converterParaDTO(clienteSalvo);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }

    }

    private void validarDuplicidade(final ClienteRequestDTO novoCliente){
        final Optional<Cliente> clienteExistente = clienteRepositorio.buscarPorCpf(novoCliente.cpf());
        if (clienteExistente.isPresent()) {
            throw new ValidacaoException("CPF duplicado");
        }
    }
}
