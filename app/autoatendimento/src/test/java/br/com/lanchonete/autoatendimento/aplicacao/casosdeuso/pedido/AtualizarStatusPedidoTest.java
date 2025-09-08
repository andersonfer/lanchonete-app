package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.gateways.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AtualizarStatusPedidoTest {

    @Mock
    private PedidoGateway pedidoGateway;

    @InjectMocks
    private AtualizarStatusPedido atualizarStatusPedido;

    private Pedido pedidoRecebido;
    private Pedido pedidoEmPreparacao;
    private Pedido pedidoPronto;

    @BeforeEach
    void configurar() {
        // Cliente para os testes
        Cliente cliente = Cliente.criar("João Silva", "joao@email.com", "12345678901");
        cliente.setId(1L);

        // Produto para os testes
        Produto produto = Produto.reconstituir(
                1L, "X-Bacon", "Hambúrguer com bacon", 
                new BigDecimal("25.90"), Categoria.LANCHE);

        // Pedido RECEBIDO com pagamento APROVADO
        pedidoRecebido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedidoRecebido.setId(1L);
        pedidoRecebido.aprovarPagamento();
        pedidoRecebido.getItens().add(ItemPedido.reconstituir(1L, pedidoRecebido, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoRecebido.setValorTotal(new BigDecimal("25.90"));

        // Pedido EM_PREPARACAO com pagamento APROVADO
        pedidoEmPreparacao = Pedido.criar(cliente, StatusPedido.EM_PREPARACAO, LocalDateTime.now().minusHours(1));
        pedidoEmPreparacao.setId(2L);
        pedidoEmPreparacao.aprovarPagamento();
        pedidoEmPreparacao.getItens().add(ItemPedido.reconstituir(2L, pedidoEmPreparacao, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoEmPreparacao.setValorTotal(new BigDecimal("25.90"));

        // Pedido PRONTO com pagamento APROVADO
        pedidoPronto = Pedido.criar(cliente, StatusPedido.PRONTO, LocalDateTime.now().minusHours(2));
        pedidoPronto.setId(3L);
        pedidoPronto.aprovarPagamento();
        pedidoPronto.getItens().add(ItemPedido.reconstituir(3L, pedidoPronto, produto, 1, 
                produto.getPreco().getValor(), new BigDecimal("25.90")));
        pedidoPronto.setValorTotal(new BigDecimal("25.90"));
    }

    @Test
    @DisplayName("Deve atualizar pedido de RECEBIDO para EM_PREPARACAO")
    void t1() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedidoRecebido));
        doNothing().when(pedidoGateway).atualizarStatus(1L, StatusPedido.EM_PREPARACAO);

        // Executar o UseCase
        atualizarStatusPedido.executar(1L, StatusPedido.EM_PREPARACAO);

        // Verificações
        verify(pedidoGateway).buscarPorId(1L);
        verify(pedidoGateway).atualizarStatus(1L, StatusPedido.EM_PREPARACAO);
    }

    @Test
    @DisplayName("Deve atualizar pedido de EM_PREPARACAO para PRONTO")
    void t2() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(2L)).thenReturn(Optional.of(pedidoEmPreparacao));
        doNothing().when(pedidoGateway).atualizarStatus(2L, StatusPedido.PRONTO);

        // Executar o UseCase
        atualizarStatusPedido.executar(2L, StatusPedido.PRONTO);

        // Verificações
        verify(pedidoGateway).buscarPorId(2L);
        verify(pedidoGateway).atualizarStatus(2L, StatusPedido.PRONTO);
    }

    @Test
    @DisplayName("Deve atualizar pedido de PRONTO para FINALIZADO")
    void t3() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(3L)).thenReturn(Optional.of(pedidoPronto));
        doNothing().when(pedidoGateway).atualizarStatus(3L, StatusPedido.FINALIZADO);

        // Executar o UseCase
        atualizarStatusPedido.executar(3L, StatusPedido.FINALIZADO);

        // Verificações
        verify(pedidoGateway).buscarPorId(3L);
        verify(pedidoGateway).atualizarStatus(3L, StatusPedido.FINALIZADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não é encontrado")
    void t4() {
        // Mock do gateway retornando vazio
        when(pedidoGateway.buscarPorId(999L)).thenReturn(Optional.empty());

        // Executar e verificar exceção
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> atualizarStatusPedido.executar(999L, StatusPedido.EM_PREPARACAO));

        assertEquals("Pedido não encontrado com ID: 999", exception.getMessage());
        verify(pedidoGateway).buscarPorId(999L);
        verify(pedidoGateway, never()).atualizarStatus(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pagamento não está aprovado")
    void t5() {
        // Criar pedido com pagamento pendente
        Pedido pedidoPendente = Pedido.criar(
                Cliente.criar("João Silva", "joao@email.com", "12345678901"),
                StatusPedido.RECEBIDO, LocalDateTime.now());
        pedidoPendente.setId(4L);
        // StatusPagamento.PENDENTE por padrão

        // Mock do gateway
        when(pedidoGateway.buscarPorId(4L)).thenReturn(Optional.of(pedidoPendente));

        // Executar e verificar exceção
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> atualizarStatusPedido.executar(4L, StatusPedido.EM_PREPARACAO));

        assertEquals("Só é possível atualizar status de pedidos com pagamento aprovado", exception.getMessage());
        verify(pedidoGateway).buscarPorId(4L);
        verify(pedidoGateway, never()).atualizarStatus(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção para transição inválida: RECEBIDO para PRONTO")
    void t6() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedidoRecebido));

        // Executar e verificar exceção
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> atualizarStatusPedido.executar(1L, StatusPedido.PRONTO));

        assertEquals("Transição inválida de RECEBIDO para PRONTO", exception.getMessage());
        verify(pedidoGateway).buscarPorId(1L);
        verify(pedidoGateway, never()).atualizarStatus(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção para transição inválida: EM_PREPARACAO para FINALIZADO")
    void t7() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(2L)).thenReturn(Optional.of(pedidoEmPreparacao));

        // Executar e verificar exceção
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> atualizarStatusPedido.executar(2L, StatusPedido.FINALIZADO));

        assertEquals("Transição inválida de EM_PREPARACAO para FINALIZADO", exception.getMessage());
        verify(pedidoGateway).buscarPorId(2L);
        verify(pedidoGateway, never()).atualizarStatus(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção para transição inválida: PRONTO para RECEBIDO")
    void t8() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(3L)).thenReturn(Optional.of(pedidoPronto));

        // Executar e verificar exceção
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> atualizarStatusPedido.executar(3L, StatusPedido.RECEBIDO));

        assertEquals("Transição inválida de PRONTO para RECEBIDO", exception.getMessage());
        verify(pedidoGateway).buscarPorId(3L);
        verify(pedidoGateway, never()).atualizarStatus(any(), any());
    }

    @Test
    @DisplayName("Deve permitir manter o mesmo status")
    void t9() {
        // Mock do gateway
        when(pedidoGateway.buscarPorId(1L)).thenReturn(Optional.of(pedidoRecebido));
        doNothing().when(pedidoGateway).atualizarStatus(1L, StatusPedido.RECEBIDO);

        // Executar o UseCase
        atualizarStatusPedido.executar(1L, StatusPedido.RECEBIDO);

        // Verificações
        verify(pedidoGateway).buscarPorId(1L);
        verify(pedidoGateway).atualizarStatus(1L, StatusPedido.RECEBIDO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido já está finalizado")
    void t10() {
        // Criar pedido finalizado
        Pedido pedidoFinalizado = Pedido.criar(
                Cliente.criar("João Silva", "joao@email.com", "12345678901"),
                StatusPedido.FINALIZADO, LocalDateTime.now());
        pedidoFinalizado.setId(5L);
        pedidoFinalizado.aprovarPagamento();

        // Mock do gateway
        when(pedidoGateway.buscarPorId(5L)).thenReturn(Optional.of(pedidoFinalizado));

        // Executar e verificar exceção
        ValidacaoException exception = assertThrows(ValidacaoException.class,
                () -> atualizarStatusPedido.executar(5L, StatusPedido.RECEBIDO));

        assertEquals("Não é possível alterar status de pedido finalizado", exception.getMessage());
        verify(pedidoGateway).buscarPorId(5L);
        verify(pedidoGateway, never()).atualizarStatus(any(), any());
    }
}