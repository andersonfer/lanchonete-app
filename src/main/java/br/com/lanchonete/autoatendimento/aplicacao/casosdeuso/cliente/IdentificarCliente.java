package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.cliente.IdentificarClienteUC;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentificarCliente implements IdentificarClienteUC {

    private final ClienteRepositorio clienteRepositorio;

    @Override
    public Optional<ClienteResponseDTO> executar(final String cpf) {

        if (cpf == null || cpf.isBlank()) {
            throw new ValidacaoException("CPF é obrigatório");
        }

         return Optional.ofNullable(clienteRepositorio.buscarPorCpf(cpf)
                 .map(ClienteResponseDTO::converterParaDTO)
                 .orElseThrow(() -> new RecursoNaoEncontradoException("CPF não encontrado")));

    }

}
