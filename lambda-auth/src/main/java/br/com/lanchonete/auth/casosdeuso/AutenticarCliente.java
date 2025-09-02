package br.com.lanchonete.auth.casosdeuso;

import br.com.lanchonete.auth.model.Cliente;
import br.com.lanchonete.auth.gateway.ClienteGateway;
import br.com.lanchonete.auth.util.CpfValidator;
import br.com.lanchonete.auth.exception.CpfInvalidoException;
import br.com.lanchonete.auth.exception.ClienteNaoEncontradoException;
import java.util.Optional;

public class AutenticarCliente {
    
    private final ClienteGateway clienteGateway;
    private final CpfValidator cpfValidator;
    
    public AutenticarCliente(ClienteGateway clienteGateway, CpfValidator cpfValidator) {
        this.clienteGateway = clienteGateway;
        this.cpfValidator = cpfValidator;
    }
    
    public Cliente executar(String cpf) throws Exception {
        if (!cpfValidator.isValido(cpf)) {
            throw new CpfInvalidoException(cpf);
        }
        
        Optional<Cliente> clienteOpt = clienteGateway.buscarPorCpf(cpf);
        return clienteOpt.orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
    }
}