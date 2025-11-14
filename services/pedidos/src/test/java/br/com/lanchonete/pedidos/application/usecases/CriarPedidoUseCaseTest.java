package br.com.lanchonete.pedidos.application.usecases;

import br.com.lanchonete.pedidos.application.gateways.ClienteGateway;
import br.com.lanchonete.pedidos.application.gateways.EventPublisher;
import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.application.gateways.ProdutoGateway;
import br.com.lanchonete.pedidos.domain.exceptions.ClienteNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.exceptions.ProdutoNaoEncontradoException;
import br.com.lanchonete.pedidos.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CriarPedidoUseCaseTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @Mock
    private ProdutoGateway produtoGateway;

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CriarPedidoUseCase criarPedidoUseCase;

    private Produto produto1;
    private Produto produto2;
    private Pedido pedidoSalvo;

    @BeforeEach
    void configurar() {
        produto1 = Produto.reconstituir(
                1L,
                "X-Burger",
                "Hambúrguer com queijo",
                new BigDecimal("15.00"),
                Categoria.LANCHE
        );

        produto2 = Produto.reconstituir(
                2L,
                "Coca-Cola",
                "Refrigerante",
                new BigDecimal("5.00"),
                Categoria.BEBIDA
        );

        pedidoSalvo = Pedido.criar(null, StatusPedido.CRIADO, java.time.LocalDateTime.now());
        pedidoSalvo.setId(1L);
        pedidoSalvo.adicionarItem(ItemPedido.criar(produto1, 2));
        pedidoSalvo.setValorTotal(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Deve criar pedido anônimo com sucesso")
    void t1() {
        CriarPedidoUseCase.ItemPedidoInput item1 = new CriarPedidoUseCase.ItemPedidoInput(1L, 2);
        List<CriarPedidoUseCase.ItemPedidoInput> itens = List.of(item1);

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto1));
        when(pedidoGateway.salvar(any(Pedido.class))).thenReturn(pedidoSalvo);

        Pedido resultado = criarPedidoUseCase.executar(null, itens);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertNull(resultado.getCpfCliente());
        verify(clienteGateway, never()).existeCliente(any());
        verify(pedidoGateway, times(1)).salvar(any(Pedido.class));
        verify(eventPublisher, times(1)).publicarPedidoCriado(1L, "30.00", null);
    }

    @Test
    @DisplayName("Deve criar pedido com CPF de cliente existente")
    void t2() {
        String cpfCliente = "12345678901";
        CriarPedidoUseCase.ItemPedidoInput item1 = new CriarPedidoUseCase.ItemPedidoInput(1L, 1);
        List<CriarPedidoUseCase.ItemPedidoInput> itens = List.of(item1);

        Pedido pedidoComCliente = Pedido.criar(cpfCliente, StatusPedido.CRIADO, java.time.LocalDateTime.now());
        pedidoComCliente.setId(2L);
        pedidoComCliente.setClienteNome("João Silva");
        pedidoComCliente.adicionarItem(ItemPedido.criar(produto1, 1));
        pedidoComCliente.setValorTotal(new BigDecimal("15.00"));

        when(clienteGateway.existeCliente(cpfCliente)).thenReturn(true);
        when(clienteGateway.buscarNomeCliente(cpfCliente)).thenReturn("João Silva");
        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto1));
        when(pedidoGateway.salvar(any(Pedido.class))).thenReturn(pedidoComCliente);

        Pedido resultado = criarPedidoUseCase.executar(cpfCliente, itens);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals(cpfCliente, resultado.getCpfCliente());
        assertEquals("João Silva", resultado.getClienteNome());
        verify(clienteGateway, times(1)).existeCliente(cpfCliente);
        verify(clienteGateway, times(1)).buscarNomeCliente(cpfCliente);
        verify(eventPublisher, times(1)).publicarPedidoCriado(2L, "15.00", cpfCliente);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não existe")
    void t3() {
        String cpfInexistente = "99999999999";
        CriarPedidoUseCase.ItemPedidoInput item1 = new CriarPedidoUseCase.ItemPedidoInput(1L, 1);
        List<CriarPedidoUseCase.ItemPedidoInput> itens = List.of(item1);

        when(clienteGateway.existeCliente(cpfInexistente)).thenReturn(false);

        ClienteNaoEncontradoException exception = assertThrows(ClienteNaoEncontradoException.class, () -> {
            criarPedidoUseCase.executar(cpfInexistente, itens);
        });

        assertNotNull(exception);
        verify(pedidoGateway, never()).salvar(any());
        verify(eventPublisher, never()).publicarPedidoCriado(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe")
    void t4() {
        CriarPedidoUseCase.ItemPedidoInput item1 = new CriarPedidoUseCase.ItemPedidoInput(999L, 1);
        List<CriarPedidoUseCase.ItemPedidoInput> itens = List.of(item1);

        when(produtoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException exception = assertThrows(ProdutoNaoEncontradoException.class, () -> {
            criarPedidoUseCase.executar(null, itens);
        });

        assertNotNull(exception);
        verify(pedidoGateway, never()).salvar(any());
        verify(eventPublisher, never()).publicarPedidoCriado(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Deve criar pedido com múltiplos itens")
    void t5() {
        CriarPedidoUseCase.ItemPedidoInput item1 = new CriarPedidoUseCase.ItemPedidoInput(1L, 2);
        CriarPedidoUseCase.ItemPedidoInput item2 = new CriarPedidoUseCase.ItemPedidoInput(2L, 3);
        List<CriarPedidoUseCase.ItemPedidoInput> itens = List.of(item1, item2);

        Pedido pedidoMultiplo = Pedido.criar(null, StatusPedido.CRIADO, java.time.LocalDateTime.now());
        pedidoMultiplo.setId(3L);
        pedidoMultiplo.adicionarItem(ItemPedido.criar(produto1, 2));
        pedidoMultiplo.adicionarItem(ItemPedido.criar(produto2, 3));
        pedidoMultiplo.setValorTotal(new BigDecimal("45.00"));

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto1));
        when(produtoGateway.buscarPorId(2L)).thenReturn(Optional.of(produto2));
        when(pedidoGateway.salvar(any(Pedido.class))).thenReturn(pedidoMultiplo);

        Pedido resultado = criarPedidoUseCase.executar(null, itens);

        assertNotNull(resultado);
        assertEquals(3L, resultado.getId());
        assertEquals(2, resultado.getItens().size());
        verify(produtoGateway, times(1)).buscarPorId(1L);
        verify(produtoGateway, times(1)).buscarPorId(2L);
        verify(eventPublisher, times(1)).publicarPedidoCriado(3L, "45.00", null);
    }

    @Test
    @DisplayName("Deve ignorar CPF vazio e criar pedido anônimo")
    void t6() {
        CriarPedidoUseCase.ItemPedidoInput item1 = new CriarPedidoUseCase.ItemPedidoInput(1L, 1);
        List<CriarPedidoUseCase.ItemPedidoInput> itens = List.of(item1);

        when(produtoGateway.buscarPorId(1L)).thenReturn(Optional.of(produto1));
        when(pedidoGateway.salvar(any(Pedido.class))).thenReturn(pedidoSalvo);

        Pedido resultado = criarPedidoUseCase.executar("   ", itens);

        assertNotNull(resultado);
        verify(clienteGateway, never()).existeCliente(any());
        verify(clienteGateway, never()).buscarNomeCliente(any());
    }
}
