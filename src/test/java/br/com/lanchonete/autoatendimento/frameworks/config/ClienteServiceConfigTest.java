package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.cliente.CadastrarCliente;
import br.com.lanchonete.autoatendimento.casosdeuso.cliente.IdentificarCliente;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ClienteServiceConfig.class})
class ClienteServiceConfigTest {

    @MockitoBean
    private ClienteGateway clienteGateway;

    @Autowired
    private CadastrarCliente cadastrarCliente;

    @Autowired
    private IdentificarCliente identificarCliente;

    @Test
    @DisplayName("Deve criar os beans dos UCs de cliente")
    void t1() {
        assertNotNull(cadastrarCliente);
        assertNotNull(identificarCliente);
    }
}