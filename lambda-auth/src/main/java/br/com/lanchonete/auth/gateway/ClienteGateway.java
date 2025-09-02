package br.com.lanchonete.auth.gateway;

import br.com.lanchonete.auth.model.Cliente;
import java.util.Optional;

public interface ClienteGateway {
    
    Optional<Cliente> buscarPorCpf(String cpf) throws Exception;
}