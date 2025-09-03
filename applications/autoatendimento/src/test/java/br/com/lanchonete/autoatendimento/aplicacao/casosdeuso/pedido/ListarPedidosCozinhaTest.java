package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
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
class ListarPedidosCozinhaTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @InjectMocks
    private ListarPedidosCozinha listarPedidosCozinha;

    private Pedido pedidoAprovado;
    private Pedido pedidoPendente;
    private Pedido pedidoRejeitado;

    @BeforeEach
    void configurar() {
        // Cliente para os testes
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");
        cliente.setId(1L);

        // Produto para os testes
        Produto produto = Produto.reconstituir(
                1L, "X-Bacon", "Hambúrguer com bacon", 
                new BigDecimal("25.90"), Categoria.LANCHE);

        // Pedido com pagamento APROVADO (deve aparecer na lista da cozinha)
        pedidoAprovado = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedidoAprovado.setId(1L);
        pedidoAprovado.aprovarPagamento(); // StatusPagamento.APROVADO
        pedidoAprovado.getItens().add(ItemPedido.reconstituir(1L, pedidoAprovado, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoAprovado.setValorTotal(new BigDecimal("25.90"));

        // Pedido com pagamento PENDENTE (não deve aparecer na lista da cozinha)
        pedidoPendente = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now().minusHours(1));
        pedidoPendente.setId(2L);
        // StatusPagamento.PENDENTE por padrão
        pedidoPendente.getItens().add(ItemPedido.reconstituir(2L, pedidoPendente, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoPendente.setValorTotal(new BigDecimal("25.90"));

        // Pedido com pagamento REJEITADO (não deve aparecer na lista da cozinha)
        pedidoRejeitado = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now().minusHours(2));
        pedidoRejeitado.setId(3L);
        pedidoRejeitado.rejeitarPagamento(); // StatusPagamento.REJEITADO
        pedidoRejeitado.getItens().add(ItemPedido.reconstituir(3L, pedidoRejeitado, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoRejeitado.setValorTotal(new BigDecimal("25.90"));
    }

    @Test
    @DisplayName("Deve listar apenas pedidos com pagamento aprovado")
    void t1() {
        // Mock do gateway retornando apenas pedidos com pagamento aprovado
        when(pedidoGateway.listarTodos()).thenReturn(Arrays.asList(pedidoAprovado));

        // Executar o UseCase
        List<Pedido> pedidos = listarPedidosCozinha.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertEquals(1, pedidos.size(), "Deve retornar apenas 1 pedido (com pagamento aprovado)");
        
        Pedido pedido = pedidos.get(0);
        assertEquals(StatusPagamento.APROVADO, pedido.getStatusPagamento(), 
                "Pedido deve ter pagamento aprovado");
        assertEquals(1L, pedido.getId(), "ID do pedido deve ser 1");

        // Verificar que o gateway foi chamado
        verify(pedidoGateway).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos com pagamento aprovado")
    void t2() {
        // Mock do gateway retornando lista vazia
        when(pedidoGateway.listarTodos()).thenReturn(Collections.emptyList());

        // Executar o UseCase
        List<Pedido> pedidos = listarPedidosCozinha.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertTrue(pedidos.isEmpty(), "A lista deve estar vazia");

        // Verificar que o gateway foi chamado
        verify(pedidoGateway).listarTodos();
    }

    @Test
    @DisplayName("Deve manter ordenação original dos pedidos com pagamento aprovado")
    void t3() {
        // Criar múltiplos pedidos com pagamento aprovado
        LocalDateTime agora = LocalDateTime.now();
        
        Cliente cliente = Cliente.criar("Maria Silva", "maria@email.com", "98765432100");
        cliente.setId(2L);
        
        Produto produto = Produto.reconstituir(
                1L, "X-Bacon", "Hambúrguer com bacon", 
                new BigDecimal("25.90"), Categoria.LANCHE);

        // Pedido PRONTO com pagamento aprovado
        Pedido pedidoPronto = Pedido.criar(cliente, StatusPedido.PRONTO, agora.minusHours(1));
        pedidoPronto.setId(4L);
        pedidoPronto.aprovarPagamento();
        pedidoPronto.getItens().add(ItemPedido.reconstituir(4L, pedidoPronto, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoPronto.setValorTotal(new BigDecimal("25.90"));

        // Pedido EM_PREPARACAO com pagamento aprovado
        Pedido pedidoEmPreparacao = Pedido.criar(cliente, StatusPedido.EM_PREPARACAO, agora.minusHours(2));
        pedidoEmPreparacao.setId(5L);
        pedidoEmPreparacao.aprovarPagamento();
        pedidoEmPreparacao.getItens().add(ItemPedido.reconstituir(5L, pedidoEmPreparacao, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoEmPreparacao.setValorTotal(new BigDecimal("25.90"));

        // Mock retornando pedidos já ordenados (PRONTO > EM_PREPARACAO)
        when(pedidoGateway.listarTodos()).thenReturn(Arrays.asList(pedidoPronto, pedidoEmPreparacao));

        // Executar o UseCase
        List<Pedido> pedidos = listarPedidosCozinha.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertEquals(2, pedidos.size(), "Deve retornar 2 pedidos");
        
        // Verificar ordenação mantida
        assertEquals(StatusPedido.PRONTO, pedidos.get(0).getStatus(), 
                "Primeiro pedido deve ter status PRONTO");
        assertEquals(StatusPedido.EM_PREPARACAO, pedidos.get(1).getStatus(), 
                "Segundo pedido deve ter status EM_PREPARACAO");

        // Verificar que todos têm pagamento aprovado
        assertTrue(pedidos.stream().allMatch(p -> p.getStatusPagamento() == StatusPagamento.APROVADO),
                "Todos os pedidos devem ter pagamento aprovado");

        // Verificar que o gateway foi chamado
        verify(pedidoGateway).listarTodos();
    }
}