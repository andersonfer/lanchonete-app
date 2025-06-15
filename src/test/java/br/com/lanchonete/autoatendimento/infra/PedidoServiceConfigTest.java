package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
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