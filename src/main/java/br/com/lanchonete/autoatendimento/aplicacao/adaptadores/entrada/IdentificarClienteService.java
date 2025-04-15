package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util.ClienteMapper;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.IdentificarClienteUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentificarClienteService implements IdentificarClienteUC {

    private final ClienteRepositorio clienteRepositorio;

    @Override
    public Optional<ClienteResponseDTO> identificar(String cpf) {

        validarParametros(cpf);

         return Optional.ofNullable(clienteRepositorio.buscarPorCpf(cpf)
                 .map(ClienteMapper::converterParaResponseDTO)
                 .orElseThrow(() -> new RecursoNaoEncontradoException("CPF não encontrado")));

    }

    private void validarParametros(String cpf) {
        if (cpf.isBlank()) {
            throw new ValidacaoException("CPF é obrigatório");
        }
    }
}
