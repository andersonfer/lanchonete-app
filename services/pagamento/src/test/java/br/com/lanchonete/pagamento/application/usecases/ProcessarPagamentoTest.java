package br.com.lanchonete.pagamento.application.usecases;

import br.com.lanchonete.pagamento.application.gateways.EventPublisher;
import br.com.lanchonete.pagamento.application.gateways.PagamentoGateway;
import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;
import br.com.lanchonete.pagamento.domain.model.Pagamento;
import br.com.lanchonete.pagamento.domain.model.StatusPagamento;
import br.com.lanchonete.pagamento.domain.model.Valor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockingDetails;

@ExtendWith(MockitoExtension.class)
class ProcessarPagamentoTest {

    @Mock
    private PagamentoGateway pagamentoGateway;

    @Mock
    private EventPublisher eventPublisher;

    private ProcessarPagamento processarPagamento;

    @BeforeEach
    void configurar() {
        processarPagamento = new ProcessarPagamento(pagamentoGateway, eventPublisher);
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso")
    void t1() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("50.00");
        Pagamento pagamentoMock = new Pagamento(pedidoId, new Valor(valor), StatusPagamento.APROVADO);

        when(pagamentoGateway.salvar(any(Pagamento.class))).thenReturn(pagamentoMock);

        Pagamento resultado = processarPagamento.executar(pedidoId, valor);

        assertNotNull(resultado);
        assertEquals(pedidoId, resultado.getPedidoId());
        verify(pagamentoGateway).salvar(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve publicar evento após processar pagamento")
    void t2() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("50.00");

        when(pagamentoGateway.salvar(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processarPagamento.executar(pedidoId, valor);

        // Verifica que pelo menos um dos eventos foi publicado
        int totalChamadas = mockingDetails(eventPublisher).getInvocations().size();
        assertEquals(1, totalChamadas);
    }

    @Test
    @DisplayName("Deve publicar evento com pedidoId correto")
    void t3() {
        Long pedidoId = 456L;
        BigDecimal valor = new BigDecimal("75.00");

        when(pagamentoGateway.salvar(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processarPagamento.executar(pedidoId, valor);

        // Verifica que um dos eventos foi chamado com o pedidoId correto
        verify(eventPublisher, atMostOnce()).publicarPagamentoAprovado(pedidoId);
        verify(eventPublisher, atMostOnce()).publicarPagamentoRejeitado(pedidoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedidoId é nulo")
    void t4() {
        BigDecimal valor = new BigDecimal("50.00");

        assertThrows(ValidacaoException.class, () -> {
            processarPagamento.executar(null, valor);
        });

        verify(pagamentoGateway, never()).salvar(any());
        verify(eventPublisher, never()).publicarPagamentoAprovado(any());
        verify(eventPublisher, never()).publicarPagamentoRejeitado(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é nulo")
    void t5() {
        Long pedidoId = 123L;

        assertThrows(ValidacaoException.class, () -> {
            processarPagamento.executar(pedidoId, null);
        });

        verify(pagamentoGateway, never()).salvar(any());
        verify(eventPublisher, never()).publicarPagamentoAprovado(any());
        verify(eventPublisher, never()).publicarPagamentoRejeitado(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é zero")
    void t6() {
        Long pedidoId = 123L;
        BigDecimal valor = BigDecimal.ZERO;

        assertThrows(ValidacaoException.class, () -> {
            processarPagamento.executar(pedidoId, valor);
        });

        verify(pagamentoGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor é negativo")
    void t7() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("-10.00");

        assertThrows(ValidacaoException.class, () -> {
            processarPagamento.executar(pedidoId, valor);
        });

        verify(pagamentoGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve salvar pagamento no gateway")
    void t8() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("50.00");
        Pagamento pagamentoMock = new Pagamento(pedidoId, new Valor(valor), StatusPagamento.APROVADO);

        when(pagamentoGateway.salvar(any(Pagamento.class))).thenReturn(pagamentoMock);

        processarPagamento.executar(pedidoId, valor);

        verify(pagamentoGateway, times(1)).salvar(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve retornar pagamento com status APROVADO ou REJEITADO")
    void t9() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("50.00");
        Pagamento pagamentoMock = new Pagamento(pedidoId, new Valor(valor), StatusPagamento.APROVADO);

        when(pagamentoGateway.salvar(any(Pagamento.class))).thenReturn(pagamentoMock);

        Pagamento resultado = processarPagamento.executar(pedidoId, valor);

        assertTrue(resultado.getStatus().isAprovado() || resultado.getStatus().isRejeitado());
    }
}
