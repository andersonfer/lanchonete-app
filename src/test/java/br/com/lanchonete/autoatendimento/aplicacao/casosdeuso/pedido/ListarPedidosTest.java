package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ListarPedidosTest {

    @Mock
    private PedidoRepositorio pedidoRepositorio;

    @InjectMocks
    private ListarPedidos listarPedidos;

    private Pedido pedidoComCliente;
    private Pedido pedidoSemCliente;

    @BeforeEach
    void configurar() {
        // Cliente para o teste
        Cliente cliente = Cliente.criar(
                "João Silva",
                "joao@email.com",
                "12345678901");
        cliente.setId(1L);

        // Produtos para o teste
        Produto produto1 = Produto.criarSemValidacao(
                1L,
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("25.90"),
                Categoria.LANCHE);

        Produto produto2 = Produto.criarSemValidacao(
                2L,
                "Refrigerante",
                "Refrigerante lata 350ml",
                new BigDecimal("6.00"),
                Categoria.BEBIDA);

        // Criar pedido com cliente
        pedidoComCliente = Pedido.criar(cliente, StatusPedido.RECEBIDO,LocalDateTime.now());
        pedidoComCliente.setId(1L);

        // Adicionar item ao pedido com cliente
        ItemPedido itemDoPedidoComCliente = ItemPedido.builder()
                .id(1L)
                .pedido(pedidoComCliente)
                .produto(produto1)
                .quantidade(2)
                .valorUnitario(produto1.getPreco())
                .valorTotal(new BigDecimal("51.80"))
                .build();
        pedidoComCliente.getItens().add(itemDoPedidoComCliente);
        pedidoComCliente.setValorTotal(new BigDecimal("51.80"));

        // Criar pedido sem cliente
        pedidoSemCliente = Pedido.criar(null,StatusPedido.FINALIZADO,LocalDateTime.now().minusHours(1));
        pedidoSemCliente.setId(2L);

        // Adicionar item ao pedido sem cliente
        ItemPedido itemDoPedidoSemCliente = ItemPedido.builder()
                .id(2L)
                .pedido(pedidoSemCliente)
                .produto(produto2)
                .quantidade(1)
                .valorUnitario(produto2.getPreco())
                .valorTotal(new BigDecimal("6.00"))
                .build();
        pedidoSemCliente.getItens().add(itemDoPedidoSemCliente);
        pedidoSemCliente.setValorTotal(new BigDecimal("6.00"));
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void t1() {
        // Mock do repositório retornando 2 pedidos
        when(pedidoRepositorio.listarTodos()).thenReturn(Arrays.asList(pedidoComCliente, pedidoSemCliente));

        // Executar o serviço
        List<PedidoResponseDTO> pedidos = listarPedidos.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertEquals(2, pedidos.size(), "Deve retornar 2 pedidos");

        // Verificar o primeiro pedido (com cliente)
        PedidoResponseDTO primeiroPedidoDTO = pedidos.get(0);
        assertEquals(1L, primeiroPedidoDTO.id(), "ID do primeiro pedido deve ser 1");
        assertEquals(1L, primeiroPedidoDTO.clienteId(), "ID do cliente deve ser 1");
        assertEquals("João Silva", primeiroPedidoDTO.nomeCliente(), "Nome do cliente deve estar correto");
        assertEquals(StatusPedido.RECEBIDO, primeiroPedidoDTO.status(), "Status deve ser RECEBIDO");
        assertEquals(1, primeiroPedidoDTO.itens().size(), "Deve ter 1 item");
        assertEquals(new BigDecimal("51.80"), primeiroPedidoDTO.valorTotal(), "Valor total deve ser 51.80");

        // Verificar o segundo pedido (sem cliente)
        PedidoResponseDTO segundoPedidoDTO = pedidos.get(1);
        assertEquals(2L, segundoPedidoDTO.id(), "ID do segundo pedido deve ser 2");
        assertNull(segundoPedidoDTO.clienteId(), "ID do cliente deve ser nulo");
        assertNull(segundoPedidoDTO.nomeCliente(), "Nome do cliente deve ser nulo");
        assertEquals(StatusPedido.FINALIZADO, segundoPedidoDTO.status(), "Status deve ser FINALIZADO");
        assertEquals(1, segundoPedidoDTO.itens().size(), "Deve ter 1 item");
        assertEquals(new BigDecimal("6.00"), segundoPedidoDTO.valorTotal(), "Valor total deve ser 6.00");

        // Verificar que o repositório foi chamado
        verify(pedidoRepositorio).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void t2() {
        // Mock do repositório retornando lista vazia
        when(pedidoRepositorio.listarTodos()).thenReturn(Collections.emptyList());

        // Executar o serviço
        List<PedidoResponseDTO> pedidos = listarPedidos.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertTrue(pedidos.isEmpty(), "A lista de pedidos deve estar vazia");

        // Verificar que o repositório foi chamado
        verify(pedidoRepositorio).listarTodos();
    }
}