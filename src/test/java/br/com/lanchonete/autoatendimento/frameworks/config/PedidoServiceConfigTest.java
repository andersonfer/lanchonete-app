package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.interfaces.ClienteGateway;
import br.com.lanchonete.autoatendimento.interfaces.PedidoGateway;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PedidoServiceConfig.class})
class PedidoServiceConfigTest {

    @MockitoBean
    private PedidoGateway pedidoGateway;

    @MockitoBean
    private ClienteGateway clienteGateway;

    @MockitoBean
    private ProdutoGateway produtoGateway;

    @Autowired
    private RealizarPedido realizarCheckout;

    @Autowired
    private ListarPedidos listarPedidos;

    @Test
    void t1() {
        assertNotNull(realizarCheckout);
        assertNotNull(listarPedidos);
    }

}