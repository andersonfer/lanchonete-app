package br.com.lanchonete.pagamento.adapters.persistence;

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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoGatewayMongoTest {

    @Mock
    private PagamentoRepository repository;

    private PagamentoGatewayMongo gateway;

    @BeforeEach
    void configurar() {
        gateway = new PagamentoGatewayMongo(repository);
    }

    @Test
    @DisplayName("Deve salvar pagamento com sucesso")
    void t1() {
        Long pedidoId = 123L;
        Valor valor = new Valor(new BigDecimal("50.00"));
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.APROVADO);

        PagamentoDocument documentMock = new PagamentoDocument(
                pedidoId,
                valor.getValor(),
                StatusPagamento.APROVADO,
                LocalDateTime.now()
        );
        documentMock.setId("507f1f77bcf86cd799439011");

        when(repository.save(any(PagamentoDocument.class))).thenReturn(documentMock);

        Pagamento resultado = gateway.salvar(pagamento);

        assertNotNull(resultado);
        assertEquals(pedidoId, resultado.getPedidoId());
        assertEquals(StatusPagamento.APROVADO, resultado.getStatus());
        verify(repository, times(1)).save(any(PagamentoDocument.class));
    }

    @Test
    @DisplayName("Deve converter Pagamento para PagamentoDocument corretamente")
    void t2() {
        Long pedidoId = 456L;
        BigDecimal valorBigDecimal = new BigDecimal("100.00");
        Valor valor = new Valor(valorBigDecimal);
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.REJEITADO);

        PagamentoDocument documentMock = new PagamentoDocument(
                pedidoId,
                valorBigDecimal,
                StatusPagamento.REJEITADO,
                LocalDateTime.now()
        );
        documentMock.setId("507f1f77bcf86cd799439012");

        when(repository.save(any(PagamentoDocument.class))).thenReturn(documentMock);

        Pagamento resultado = gateway.salvar(pagamento);

        assertEquals(pedidoId, resultado.getPedidoId());
        assertEquals(valorBigDecimal, resultado.getValor());
        assertEquals(StatusPagamento.REJEITADO, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve retornar pagamento com ID após salvar")
    void t3() {
        Long pedidoId = 789L;
        Valor valor = new Valor(new BigDecimal("75.50"));
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.APROVADO);

        String idEsperado = "507f1f77bcf86cd799439013";
        PagamentoDocument documentMock = new PagamentoDocument(
                pedidoId,
                valor.getValor(),
                StatusPagamento.APROVADO,
                LocalDateTime.now()
        );
        documentMock.setId(idEsperado);

        when(repository.save(any(PagamentoDocument.class))).thenReturn(documentMock);

        Pagamento resultado = gateway.salvar(pagamento);

        assertEquals(idEsperado, resultado.getId());
    }
}
