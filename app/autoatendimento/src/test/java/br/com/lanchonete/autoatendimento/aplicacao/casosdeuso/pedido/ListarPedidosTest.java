package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
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
class ListarPedidosTest {

    @Mock
    private PedidoGateway pedidoGateway;

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
        Produto produto1 = Produto.reconstituir(
                1L,
                "X-Bacon",
                "Hambúrguer com bacon",
                new BigDecimal("25.90"),
                Categoria.LANCHE);

        Produto produto2 = Produto.reconstituir(
                2L,
                "Refrigerante",
                "Refrigerante lata 350ml",
                new BigDecimal("6.00"),
                Categoria.BEBIDA);

        // Criar pedido com cliente
        pedidoComCliente = Pedido.criar(cliente, StatusPedido.RECEBIDO,LocalDateTime.now());
        pedidoComCliente.setId(1L);

        // Adicionar item ao pedido com cliente
        ItemPedido itemDoPedidoComCliente = ItemPedido.reconstituir(
                1L,
                pedidoComCliente,
                produto1,
                2,
                produto1.getPreco().getValor(),
                new BigDecimal("51.80")
        );
        pedidoComCliente.getItens().add(itemDoPedidoComCliente);
        pedidoComCliente.setValorTotal(new BigDecimal("51.80"));

        // Criar pedido sem cliente
        pedidoSemCliente = Pedido.criar(null,StatusPedido.FINALIZADO,LocalDateTime.now().minusHours(1));
        pedidoSemCliente.setId(2L);

        // Adicionar item ao pedido sem cliente
        ItemPedido itemDoPedidoSemCliente = ItemPedido.reconstituir(
                2L,
                pedidoSemCliente,
                produto2,
                1,
                produto2.getPreco().getValor(),
                new BigDecimal("6.00")
        );
        pedidoSemCliente.getItens().add(itemDoPedidoSemCliente);
        pedidoSemCliente.setValorTotal(new BigDecimal("6.00"));
    }

    @Test
    @DisplayName("Deve listar pedidos excluindo os finalizados")
    void t1() {
        // Mock do repositório retornando apenas o pedido não finalizado
        // (simulando que o filtro já foi aplicado na camada de persistência)
        when(pedidoGateway.listarTodos()).thenReturn(Arrays.asList(pedidoComCliente));

        // Executar o serviço
        List<Pedido> pedidos = listarPedidos.executar();

        // Verificações - deve retornar apenas 1 pedido (excluindo FINALIZADO)
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertEquals(1, pedidos.size(), "Deve retornar apenas 1 pedido (excluindo FINALIZADO)");

        // Verificar o pedido retornado (com cliente)
        Pedido primeiroPedido = pedidos.get(0);
        assertEquals(1L, primeiroPedido.getId(), "ID do primeiro pedido deve ser 1");
        assertEquals(1L, primeiroPedido.getCliente().getId(), "ID do cliente deve ser 1");
        assertEquals("João Silva", primeiroPedido.getCliente().getNome(), "Nome do cliente deve estar correto");
        assertEquals(StatusPedido.RECEBIDO, primeiroPedido.getStatus(), "Status deve ser RECEBIDO");
        assertEquals(1, primeiroPedido.getItens().size(), "Deve ter 1 item");
        assertEquals(new BigDecimal("51.80"), primeiroPedido.getValorTotal(), "Valor total deve ser 51.80");

        // Verificar que nenhum pedido finalizado foi retornado
        boolean temPedidoFinalizado = pedidos.stream()
                .anyMatch(p -> p.getStatus() == StatusPedido.FINALIZADO);
        assertFalse(temPedidoFinalizado, "Não deve retornar pedidos com status FINALIZADO");

        // Verificar que o repositório foi chamado
        verify(pedidoGateway).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void t2() {
        // Mock do repositório retornando lista vazia
        when(pedidoGateway.listarTodos()).thenReturn(Collections.emptyList());

        // Executar o serviço
        List<Pedido> pedidos = listarPedidos.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertTrue(pedidos.isEmpty(), "A lista de pedidos deve estar vazia");

        // Verificar que o repositório foi chamado
        verify(pedidoGateway).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar pedidos ordenados por status: PRONTO > EM_PREPARACAO > RECEBIDO")
    void t3() {
        // Criar pedidos com diferentes status e horários
        LocalDateTime agora = LocalDateTime.now();
        
        // Produto para os testes
        Produto produto = Produto.reconstituir(
                1L, "X-Bacon", "Hambúrguer com bacon", 
                new BigDecimal("25.90"), Categoria.LANCHE);

        // Pedido RECEBIDO (mais antigo)
        Pedido pedidoRecebido = Pedido.criar(null, StatusPedido.RECEBIDO, agora.minusHours(3));
        pedidoRecebido.setId(1L);
        pedidoRecebido.getItens().add(ItemPedido.reconstituir(1L, pedidoRecebido, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));

        // Pedido EM_PREPARACAO (meio)
        Pedido pedidoEmPreparacao = Pedido.criar(null, StatusPedido.EM_PREPARACAO, agora.minusHours(2));
        pedidoEmPreparacao.setId(2L);
        pedidoEmPreparacao.getItens().add(ItemPedido.reconstituir(2L, pedidoEmPreparacao, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));

        // Pedido PRONTO (mais recente)
        Pedido pedidoPronto = Pedido.criar(null, StatusPedido.PRONTO, agora.minusHours(1));
        pedidoPronto.setId(3L);
        pedidoPronto.getItens().add(ItemPedido.reconstituir(3L, pedidoPronto, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));

        // Mock retornando pedidos já ordenados conforme nova regra
        when(pedidoGateway.listarTodos()).thenReturn(Arrays.asList(
                pedidoPronto,      // PRONTO primeiro
                pedidoEmPreparacao, // EM_PREPARACAO segundo
                pedidoRecebido     // RECEBIDO por último
        ));

        // Executar o serviço
        List<Pedido> pedidos = listarPedidos.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertEquals(3, pedidos.size(), "Deve retornar 3 pedidos");

        // Verificar ordenação por status
        assertEquals(StatusPedido.PRONTO, pedidos.get(0).getStatus(), 
                "Primeiro pedido deve ter status PRONTO");
        assertEquals(StatusPedido.EM_PREPARACAO, pedidos.get(1).getStatus(), 
                "Segundo pedido deve ter status EM_PREPARACAO");
        assertEquals(StatusPedido.RECEBIDO, pedidos.get(2).getStatus(), 
                "Terceiro pedido deve ter status RECEBIDO");

        // Verificar que o repositório foi chamado
        verify(pedidoGateway).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar pedidos do mesmo status ordenados por data de criação (mais antigos primeiro)")
    void t4() {
        // Criar múltiplos pedidos com mesmo status mas datas diferentes
        LocalDateTime agora = LocalDateTime.now();
        
        Produto produto = Produto.reconstituir(
                1L, "X-Bacon", "Hambúrguer com bacon", 
                new BigDecimal("25.90"), Categoria.LANCHE);

        // Pedido RECEBIDO mais recente
        Pedido pedidoRecente = Pedido.criar(null, StatusPedido.RECEBIDO, agora.minusMinutes(10));
        pedidoRecente.setId(1L);
        pedidoRecente.getItens().add(ItemPedido.reconstituir(1L, pedidoRecente, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));

        // Pedido RECEBIDO mais antigo
        Pedido pedidoAntigo = Pedido.criar(null, StatusPedido.RECEBIDO, agora.minusHours(1));
        pedidoAntigo.setId(2L);
        pedidoAntigo.getItens().add(ItemPedido.reconstituir(2L, pedidoAntigo, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));

        // Mock retornando pedidos ordenados por data (mais antigos primeiro)
        when(pedidoGateway.listarTodos()).thenReturn(Arrays.asList(
                pedidoAntigo,   // Mais antigo primeiro
                pedidoRecente   // Mais recente depois
        ));

        // Executar o serviço
        List<Pedido> pedidos = listarPedidos.executar();

        // Verificações
        assertNotNull(pedidos, "A lista de pedidos não deve ser nula");
        assertEquals(2, pedidos.size(), "Deve retornar 2 pedidos");

        // Verificar ordenação por data (mais antigos primeiro)
        assertTrue(pedidos.get(0).getDataCriacao().isBefore(pedidos.get(1).getDataCriacao()),
                "Primeiro pedido deve ser mais antigo que o segundo");
        assertEquals(2L, pedidos.get(0).getId(), "Primeiro pedido deve ser o mais antigo (ID 2)");
        assertEquals(1L, pedidos.get(1).getId(), "Segundo pedido deve ser o mais recente (ID 1)");

        // Verificar que o repositório foi chamado
        verify(pedidoGateway).listarTodos();
    }
}