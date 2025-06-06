package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.pedido.ListarPedidos;
import br.com.lanchonete.autoatendimento.casosdeuso.pedido.RealizarPedido;
import br.com.lanchonete.autoatendimento.interfaces.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.interfaces.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoRepositorio;
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
    private PedidoRepositorio pedidoRepositorio;

    @MockitoBean
    private ClienteRepositorio clienteRepositorio;

    @MockitoBean
    private ProdutoRepositorio produtoRepositorio;

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