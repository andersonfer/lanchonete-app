package br.com.lanchonete.auth.adapters.mock;

import br.com.lanchonete.auth.application.gateways.ClienteGateway;
import br.com.lanchonete.auth.domain.entities.Cliente;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClienteMockGateway implements ClienteGateway {
    
    private static final List<Cliente> CLIENTES_MOCK = Arrays.asList(
        Cliente.reconstituir(1L, "Maria Oliveira", "maria.oliveira@email.com", "12345678901"),
        Cliente.reconstituir(2L, "João Silva", "joao.silva@email.com", "11144477735"),  
        Cliente.reconstituir(3L, "Pedro Costa", "pedro.costa@email.com", "98765432100"),
        Cliente.reconstituir(4L, "Ana Santos", "ana.santos@email.com", "11111111111"),
        Cliente.reconstituir(5L, "Carlos Ferreira", "carlos.ferreira@email.com", "22222222222")
    );

    @Override
    public Cliente salvar(Cliente cliente) {
        // Mock implementation - não implementado para este caso de uso
        throw new UnsupportedOperationException("Operação não implementada no mock");
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return CLIENTES_MOCK.stream()
                .filter(cliente -> cliente.getCpf().getValor().equals(cpf))
                .findFirst();
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return CLIENTES_MOCK.stream()
                .filter(cliente -> cliente.getId().equals(id))
                .findFirst();
    }
}