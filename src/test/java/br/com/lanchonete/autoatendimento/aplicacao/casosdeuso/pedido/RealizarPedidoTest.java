package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RealizarPedidoTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private ProdutoGateway produtoGateway;

    @InjectMocks
    private RealizarPedido realizarPedido;

    private Cliente cliente;
    private String cpfCliente;
    private List<ItemPedidoInfo> itens;

    @BeforeEach
    void configurar() {
        // Cliente
        cliente = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");

        // Produtos
        Produto produto1 = Produto.reconstituir(1L, "X-Bacon", "Hambúrguer com bacon",
                new BigDecimal("25.90"), Categoria.LANCHE);

        Produto produto2 = Produto.reconstituir(2L, "Refrigerante", "Refrigerante lata 350ml",
                new BigDecimal("6.00"), Categoria.BEBIDA);

        // Mock para busca de cliente por CPF
        when(clienteGateway.buscarPorCpf("12345678901")).thenReturn(Optional.of(cliente));

        // Mock para busca de produtos por ID
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto1));
        when(produtoGateway.buscarPorId(2L)).thenReturn(Optional.of(produto2));

        // Mock para salvar pedido (retorna o mesmo objeto)
        when(pedidoGateway.salvar(any(Pedido.class))).thenAnswer(i -> {
            Pedido p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Configurar dados para teste
        cpfCliente = "12345678901";
        itens = Arrays.asList(
                new ItemPedidoInfo(1L, 2),
                new ItemPedidoInfo(2L, 1)
        );
    }

    @Test
    @DisplayName("Deve realizar checkout de pedido com cliente com sucesso")
    void t1() {
        // Executar o checkout
        Pedido pedidoRetornado = realizarPedido.executar(cpfCliente, itens);

        // Verificações
        assertNotNull(pedidoRetornado, "O pedido retornado não deveria ser nulo");
        assertEquals(1L, pedidoRetornado.getId(), "O ID do pedido deve ser 1");
        assertEquals(cliente, pedidoRetornado.getCliente(), "O cliente deve ser o mesmo");
        assertEquals("João Silva", pedidoRetornado.getCliente().getNome(), "O nome do cliente deve estar correto");
        assertEquals(2, pedidoRetornado.getItens().size(), "O pedido deve ter 2 itens");
        assertEquals(StatusPedido.RECEBIDO, pedidoRetornado.getStatus(), "O status deve ser RECEBIDO");
        assertEquals(new BigDecimal("57.80"), pedidoRetornado.getValorTotal(),
                "O valor total deve ser 2 * 25.90 + 1 * 6.00 = 57.80");

        // Verificar chamada para salvar pedido
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoGateway).salvar(pedidoCaptor.capture());

        Pedido pedidoSalvo = pedidoCaptor.getValue();
        assertEquals(cliente, pedidoSalvo.getCliente(), "O cliente deve ser o mesmo");
        assertEquals(StatusPedido.RECEBIDO, pedidoSalvo.getStatus(), "O status deve ser RECEBIDO");
        assertEquals(2, pedidoSalvo.getItens().size(), "O pedido deve ter 2 itens");
    }

    @Test
    @DisplayName("Deve realizar checkout de pedido sem cliente com sucesso")
    void t2() {
        List<ItemPedidoInfo> itensSemCliente = Arrays.asList(
                new ItemPedidoInfo(1L, 1)
        );

        // Executar o checkout
        Pedido pedidoRetornado = realizarPedido.executar(null, itensSemCliente);

        // Verificações
        assertNotNull(pedidoRetornado, "O pedido retornado não deveria ser nulo");
        assertEquals(1L, pedidoRetornado.getId(), "O ID do pedido deve ser 1");
        assertNull(pedidoRetornado.getCliente(), "O cliente deve ser nulo");
        assertEquals(1, pedidoRetornado.getItens().size(), "O pedido deve ter 1 item");
        assertEquals(StatusPedido.RECEBIDO, pedidoRetornado.getStatus(), "O status deve ser RECEBIDO");
        assertEquals(new BigDecimal("25.90"), pedidoRetornado.getValorTotal(),
                "O valor total deve ser igual ao preço do produto");

        // Verificar que não buscou cliente
        verify(clienteGateway, never()).buscarPorCpf(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout com cliente inexistente")
    void t3() {
        List<ItemPedidoInfo> itensTestCliente = Arrays.asList(
                new ItemPedidoInfo(1L, 1)
        );

        // Mock para retornar cliente não encontrado
        when(clienteGateway.buscarPorCpf("99999999999")).thenReturn(Optional.empty());

        // Verificar exceção
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> realizarPedido.executar("99999999999", itensTestCliente),
                "Deveria lançar exceção para cliente não encontrado"
        );

        assertEquals("Cliente não encontrado com o CPF informado", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout com produto inexistente")
    void t4() {
        List<ItemPedidoInfo> itensTestProduto = Arrays.asList(
                new ItemPedidoInfo(999L, 1)
        );

        // Mock para retornar produto não encontrado
        when(produtoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        // Verificar exceção
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> realizarPedido.executar(null, itensTestProduto),
                "Deveria lançar exceção para produto não encontrado"
        );

        assertEquals("Produto não encontrado: 999", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout sem itens")
    void t5() {
        List<ItemPedidoInfo> itensSemItens = List.of();

        // Verificar exceção
        ValidacaoException exception = assertThrows(
                ValidacaoException.class,
                () -> realizarPedido.executar(null, itensSemItens),
                "Deveria lançar exceção para pedido sem itens"
        );

        assertEquals("Pedido deve conter pelo menos um item", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar checkout com quantidade inválida")
    void t6() {
        List<ItemPedidoInfo> itensQuantidadeZero = Arrays.asList(
                new ItemPedidoInfo(1L, 0)
        );

        // Verificar exceção
        ValidacaoException exception = assertThrows(
                ValidacaoException.class,
                () -> realizarPedido.executar(null, itensQuantidadeZero),
                "Deveria lançar exceção para quantidade zero"
        );

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage(),
                "Mensagem de erro incorreta");

        // Verificar que não tentou salvar pedido
        verify(pedidoGateway, never()).salvar(any());
    }
}