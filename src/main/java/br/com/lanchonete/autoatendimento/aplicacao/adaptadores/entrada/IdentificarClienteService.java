package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util.ClienteMapper;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.IdentificarClienteUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdentificarClienteService implements IdentificarClienteUC {

    private final ClienteRepositorio clienteRepositorio;

    @Override
    public Optional<ClienteResponseDTO> identificar(String cpf) {
        if (cpf.isBlank()) {
            throw new ValidacaoException("CPF é obrigatório");
        }
         return clienteRepositorio.buscarPorCpf(cpf)
                 .map(ClienteMapper::converterParaResponseDTO);
    }
}
