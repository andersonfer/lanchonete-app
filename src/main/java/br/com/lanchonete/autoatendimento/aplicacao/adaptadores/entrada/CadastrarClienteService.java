package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.util.ClienteMapper;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.CadastrarClienteUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Cliente;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CadastrarClienteService implements CadastrarClienteUC {

    private final ClienteRepositorio clienteRepositorio;


    @Override
    public ClienteResponseDTO cadastrar(CadastrarClienteDTO novoCliente) {

        validarParametros(novoCliente);
        validarDuplicidade(novoCliente);

        Cliente cliente = Cliente.builder()
                .nome(novoCliente.getNome())
                .email(novoCliente.getEmail())
                .cpf(novoCliente.getCpf())
                .build();

        Cliente clienteSalvo = clienteRepositorio.salvar(cliente);

        return ClienteMapper.converterParaResponseDTO(clienteSalvo);

    }

    private void validarParametros(CadastrarClienteDTO novoCliente) {
        if (StringUtils.isBlank(novoCliente.getNome())) {
            throw new ValidacaoException("Nome é obrigatório");
        }
        if (StringUtils.isBlank(novoCliente.getEmail())) {
            throw new ValidacaoException("Email é obrigatório");
        }
        if (!novoCliente.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidacaoException("Email inválido");
        }
        if (StringUtils.isBlank(novoCliente.getCpf())) {
            throw new ValidacaoException("CPF é obrigatório");
        }
        if (!novoCliente.getCpf().matches("^\\d{11}$")) {
            throw new ValidacaoException("CPF deve conter 11 dígitos numéricos");
        }
    }

    private void validarDuplicidade(CadastrarClienteDTO novoCliente){
        Optional<Cliente> clienteExistente = clienteRepositorio.buscarPorCpf(novoCliente.getCpf());
        if (clienteExistente.isPresent()) {
            throw new ValidacaoException("CPF duplicado");
        }
    }
}
