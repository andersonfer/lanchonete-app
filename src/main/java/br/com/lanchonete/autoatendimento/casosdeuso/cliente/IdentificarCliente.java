package br.com.lanchonete.autoatendimento.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentificarCliente {

    private final ClienteGateway clienteGateway;

    public Optional<ClienteResponseDTO> executar(final String cpf) {

        if (cpf == null || cpf.isBlank()) {
            throw new ValidacaoException("CPF é obrigatório");
        }

         return Optional.ofNullable(clienteGateway.buscarPorCpf(cpf)
                 .map(ClienteResponseDTO::converterParaDTO)
                 .orElseThrow(() -> new RecursoNaoEncontradoException("CPF não encontrado")));

    }

}
