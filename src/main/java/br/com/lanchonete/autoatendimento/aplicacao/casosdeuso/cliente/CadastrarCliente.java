package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.cliente;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Cliente;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CadastrarCliente implements CadastrarClienteCasoDeUso {

    private final ClienteRepositorio clienteRepositorio;


    @Override
    public ClienteResponseDTO executar(ClienteRequestDTO novoCliente) {

        validarParametros(novoCliente);
        validarDuplicidade(novoCliente);

        Cliente cliente = Cliente.builder()
                .nome(novoCliente.nome())
                .email(novoCliente.email())
                .cpf(novoCliente.cpf())
                .build();

        Cliente clienteSalvo = clienteRepositorio.salvar(cliente);

        return ClienteResponseDTO.converterParaDTO(clienteSalvo);

    }

    private void validarParametros(ClienteRequestDTO novoCliente) {
        if (StringUtils.isBlank(novoCliente.nome())) {
            throw new ValidacaoException("Nome é obrigatório");
        }
        if (StringUtils.isBlank(novoCliente.email())) {
            throw new ValidacaoException("Email é obrigatório");
        }
        if (!novoCliente.email().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidacaoException("Email inválido");
        }
        if (StringUtils.isBlank(novoCliente.cpf())) {
            throw new ValidacaoException("CPF é obrigatório");
        }
        if (!novoCliente.cpf().matches("^\\d{11}$")) {
            throw new ValidacaoException("CPF deve conter 11 dígitos numéricos");
        }
    }

    private void validarDuplicidade(ClienteRequestDTO novoCliente){
        Optional<Cliente> clienteExistente = clienteRepositorio.buscarPorCpf(novoCliente.cpf());
        if (clienteExistente.isPresent()) {
            throw new ValidacaoException("CPF duplicado");
        }
    }
}
