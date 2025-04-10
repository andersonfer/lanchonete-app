package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.CadastrarClienteUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Cliente;
import io.swagger.v3.oas.annotations.servers.Server;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Server
public class CadastrarClienteService implements CadastrarClienteUC {

    private final ClienteRepositorio clienteRepositorio;

    public CadastrarClienteService(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    @Override
    public ClienteResponseDTO cadastrar(CadastrarClienteDTO novoCliente) {

        validarDadosCliente(novoCliente);

        Optional<Cliente> clienteExistente = clienteRepositorio.buscarPorCpf(novoCliente.getCpf());
        if (clienteExistente.isPresent()) {
            throw new IllegalArgumentException("CPF duplicado");
        }


        Cliente cliente = Cliente.builder()
                .nome(novoCliente.getNome())
                .email(novoCliente.getEmail())
                .cpf(novoCliente.getCpf())
                .build();

        Cliente clienteSalvo = clienteRepositorio.salvar(cliente);

        return converterParaResponseDTO(clienteSalvo);

    }

    private void validarDadosCliente(CadastrarClienteDTO novoCliente) {
        if (StringUtils.isBlank(novoCliente.getNome())) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (StringUtils.isBlank(novoCliente.getEmail())) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (!novoCliente.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (StringUtils.isBlank(novoCliente.getCpf())) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (!novoCliente.getCpf().matches("^\\d{11}$")) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos numéricos");
        }
    }

    private ClienteResponseDTO converterParaResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .cpf(cliente.getCpf())
                .build();
    }
}
