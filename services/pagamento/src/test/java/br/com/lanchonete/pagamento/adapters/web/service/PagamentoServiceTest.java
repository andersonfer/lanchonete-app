package br.com.lanchonete.pagamento.adapters.web.service;

import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoRequest;
import br.com.lanchonete.pagamento.adapters.web.dto.PagamentoResponse;
import br.com.lanchonete.pagamento.application.usecases.ProcessarPagamento;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private ProcessarPagamento processarPagamento;

    private PagamentoService pagamentoService;

    @BeforeEach
    void configurar() {
        pagamentoService = new PagamentoService(processarPagamento);
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso")
    void t1() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("50.00");
        PagamentoRequest request = new PagamentoRequest(pedidoId, valor);

        Pagamento pagamentoMock = new Pagamento(pedidoId, new Valor(valor), StatusPagamento.APROVADO);
        pagamentoMock.setId("507f1f77bcf86cd799439011");

        when(processarPagamento.executar(eq(pedidoId), eq(valor))).thenReturn(pagamentoMock);

        PagamentoResponse response = pagamentoService.processar(request);

        assertNotNull(response);
        assertEquals("507f1f77bcf86cd799439011", response.id());
        assertEquals(pedidoId, response.pedidoId());
        assertEquals("APROVADO", response.status());
        verify(processarPagamento, times(1)).executar(pedidoId, valor);
    }

    @Test
    @DisplayName("Deve converter Pagamento para PagamentoResponse corretamente")
    void t2() {
        Long pedidoId = 456L;
        BigDecimal valor = new BigDecimal("100.00");
        PagamentoRequest request = new PagamentoRequest(pedidoId, valor);

        Pagamento pagamentoMock = new Pagamento(pedidoId, new Valor(valor), StatusPagamento.REJEITADO);
        pagamentoMock.setId("507f1f77bcf86cd799439012");

        when(processarPagamento.executar(any(), any())).thenReturn(pagamentoMock);

        PagamentoResponse response = pagamentoService.processar(request);

        assertEquals("REJEITADO", response.status());
    }

    @Test
    @DisplayName("Deve chamar use case com parâmetros corretos")
    void t3() {
        Long pedidoId = 789L;
        BigDecimal valor = new BigDecimal("75.50");
        PagamentoRequest request = new PagamentoRequest(pedidoId, valor);

        Pagamento pagamentoMock = new Pagamento(pedidoId, new Valor(valor), StatusPagamento.APROVADO);

        when(processarPagamento.executar(pedidoId, valor)).thenReturn(pagamentoMock);

        pagamentoService.processar(request);

        verify(processarPagamento).executar(pedidoId, valor);
    }
}
