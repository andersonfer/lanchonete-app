package br.com.lanchonete.auth.gateway;

import br.com.lanchonete.auth.model.Cliente;
import br.com.lanchonete.auth.exception.ClienteNaoEncontradoException;
import java.util.Optional;

public interface ClienteGateway {
    
    Optional<Cliente> buscarPorCpf(String cpf) throws Exception;
    
    default Cliente buscarPorCpfObrigatorio(String cpf) throws Exception {
        return buscarPorCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException(cpf));
    }
}