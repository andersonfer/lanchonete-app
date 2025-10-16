package br.com.lanchonete.clientes.infrastructure.config;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.application.usecases.BuscarClientePorCpf;
import br.com.lanchonete.clientes.application.usecases.CadastrarCliente;
import br.com.lanchonete.clientes.application.usecases.IdentificarCliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UseCaseConfig.class})
class UseCaseConfigTest {

    @MockitoBean
    private ClienteGateway clienteGateway;

    @Autowired
    private CadastrarCliente cadastrarCliente;

    @Autowired
    private IdentificarCliente identificarCliente;

    @Autowired
    private BuscarClientePorCpf buscarClientePorCpf;

    @Test
    @DisplayName("Deve criar os beans dos UCs de cliente")
    void t1() {
        assertNotNull(cadastrarCliente);
        assertNotNull(identificarCliente);
        assertNotNull(buscarClientePorCpf);
    }
}
