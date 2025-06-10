package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RepositorioConfig.class, JdbcConfig.class})
class RepositorioConfigTest {

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClienteGateway clienteGateway;

    @Autowired
    private ProdutoGateway produtoGateway;

    @Autowired
    private PedidoGateway pedidoGateway;

    @Test
    @DisplayName( "Deve criar os beans de repositorio")
    void t1() {
        assertNotNull(clienteGateway);
        assertNotNull(produtoGateway);
        assertNotNull(pedidoGateway);
    }
}