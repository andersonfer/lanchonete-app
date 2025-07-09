package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.webhook;

import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarWebhookPagamentoTest {

    @Mock
    private PedidoGateway pedidoGateway;

    private ProcessarWebhookPagamento processarWebhookPagamento;

    @BeforeEach
    void configurar() {
        processarWebhookPagamento = new ProcessarWebhookPagamento(pedidoGateway);
    }

    @Test
    @DisplayName("Deve aprovar pagamento quando pedido existir e estiver pendente")
    void t1() {
        // Arrange
        Long pedidoId = 1L;
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(pedidoId);
        
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));
        
        // Act
        processarWebhookPagamento.processar(pedidoId, StatusPagamento.APROVADO);
        
        // Assert
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway).atualizarStatusPagamento(pedidoId, StatusPagamento.APROVADO);
        assertEquals(StatusPagamento.APROVADO, pedido.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve rejeitar pagamento quando pedido existir e estiver pendente")
    void t2() {
        // Arrange
        Long pedidoId = 1L;
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(pedidoId);
        
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));
        
        // Act
        processarWebhookPagamento.processar(pedidoId, StatusPagamento.REJEITADO);
        
        // Assert
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway).atualizarStatusPagamento(pedidoId, StatusPagamento.REJEITADO);
        assertEquals(StatusPagamento.REJEITADO, pedido.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existir")
    void t3() {
        // Arrange
        Long pedidoId = 999L;
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.empty());
        
        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(
            RecursoNaoEncontradoException.class,
            () -> processarWebhookPagamento.processar(pedidoId, StatusPagamento.APROVADO)
        );
        
        assertEquals("Pedido não encontrado: 999", exception.getMessage());
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido já estiver aprovado")
    void t4() {
        // Arrange
        Long pedidoId = 1L;
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(pedidoId);
        pedido.aprovarPagamento();
        
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));
        
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(pedidoId, StatusPagamento.APROVADO)
        );
        
        assertEquals("Pagamento do pedido 1 já foi processado", exception.getMessage());
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido já estiver rejeitado")
    void t5() {
        // Arrange
        Long pedidoId = 1L;
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(pedidoId);
        pedido.rejeitarPagamento();
        
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));
        
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(pedidoId, StatusPagamento.REJEITADO)
        );
        
        assertEquals("Pagamento do pedido 1 já foi processado", exception.getMessage());
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar mudar para status PENDENTE")
    void t6() {
        // Arrange
        Long pedidoId = 1L;
        
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(pedidoId, StatusPagamento.PENDENTE)
        );
        
        assertEquals("Status de pagamento inválido: PENDENTE", exception.getMessage());
        verify(pedidoGateway, never()).buscarPorId(any());
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedidoId for null")
    void t7() {
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(null, StatusPagamento.APROVADO)
        );
        
        assertEquals("PedidoId não pode ser null", exception.getMessage());
        verify(pedidoGateway, never()).buscarPorId(any());
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando statusPagamento for null")
    void t8() {
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(1L, null)
        );
        
        assertEquals("StatusPagamento não pode ser null", exception.getMessage());
        verify(pedidoGateway, never()).buscarPorId(any());
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve ser idempotente - aprovar pedido já aprovado deve falhar")
    void t9() {
        // Arrange
        Long pedidoId = 1L;
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(pedidoId);
        pedido.aprovarPagamento();
        
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));
        
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(pedidoId, StatusPagamento.APROVADO)
        );
        
        assertEquals("Pagamento do pedido 1 já foi processado", exception.getMessage());
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }

    @Test
    @DisplayName("Deve ser idempotente - rejeitar pedido já rejeitado deve falhar")
    void t10() {
        // Arrange
        Long pedidoId = 1L;
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setId(pedidoId);
        pedido.rejeitarPagamento();
        
        when(pedidoGateway.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));
        
        // Act & Assert
        ValidacaoException exception = assertThrows(
            ValidacaoException.class,
            () -> processarWebhookPagamento.processar(pedidoId, StatusPagamento.REJEITADO)
        );
        
        assertEquals("Pagamento do pedido 1 já foi processado", exception.getMessage());
        verify(pedidoGateway).buscarPorId(pedidoId);
        verify(pedidoGateway, never()).atualizarStatusPagamento(any(), any());
    }
}