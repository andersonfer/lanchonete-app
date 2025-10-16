package br.com.lanchonete.clientes.adapters.web.service;

import br.com.lanchonete.clientes.adapters.web.dto.ClienteRequest;
import br.com.lanchonete.clientes.adapters.web.dto.ClienteResponse;
import br.com.lanchonete.clientes.application.usecases.BuscarClientePorCpf;
import br.com.lanchonete.clientes.application.usecases.CadastrarCliente;
import br.com.lanchonete.clientes.application.usecases.IdentificarCliente;
import br.com.lanchonete.clientes.domain.model.Cliente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final CadastrarCliente cadastrarCliente;
    private final IdentificarCliente identificarCliente;
    private final BuscarClientePorCpf buscarClientePorCpf;

    public ClienteService(
            final CadastrarCliente cadastrarCliente,
            final IdentificarCliente identificarCliente,
            final BuscarClientePorCpf buscarClientePorCpf
    ) {
        this.cadastrarCliente = cadastrarCliente;
        this.identificarCliente = identificarCliente;
        this.buscarClientePorCpf = buscarClientePorCpf;
    }

    @Transactional
    public ClienteResponse cadastrar(final ClienteRequest request) {
        final Cliente cliente = cadastrarCliente.executar(
                request.nome(),
                request.email(),
                request.cpf()
        );
        return toResponse(cliente);
    }

    public ClienteResponse identificar(final String cpf) {
        final Cliente cliente = identificarCliente.executar(cpf);
        return toResponse(cliente);
    }

    public ClienteResponse buscarPorCpf(final String cpf) {
        final Cliente cliente = buscarClientePorCpf.executar(cpf);
        return toResponse(cliente);
    }

    private ClienteResponse toResponse(final Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail().getValor(),
                cliente.getCpf().getValor()
        );
    }
}
