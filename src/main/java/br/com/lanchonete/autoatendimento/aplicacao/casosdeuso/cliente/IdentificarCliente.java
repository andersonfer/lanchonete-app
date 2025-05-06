package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.repositorios.ClienteRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentificarCliente implements IdentificarClienteCasoDeUso {

    private final ClienteRepositorio clienteRepositorio;

    @Override
    public Optional<ClienteResponseDTO> executar(String cpf) {

        validarParametros(cpf);

         return Optional.ofNullable(clienteRepositorio.buscarPorCpf(cpf)
                 .map(ClienteResponseDTO::converterParaDTO)
                 .orElseThrow(() -> new RecursoNaoEncontradoException("CPF não encontrado")));

    }

    private void validarParametros(String cpf) {
        if (cpf.isBlank()) {
            throw new ValidacaoException("CPF é obrigatório");
        }
    }
}
