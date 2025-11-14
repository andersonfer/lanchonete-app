package br.com.lanchonete.pagamento.adapters.messaging;

import br.com.lanchonete.pagamento.application.usecases.ProcessarPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoCriadoConsumerTest {

    @Mock
    private ProcessarPagamento processarPagamento;

    private PedidoCriadoConsumer consumer;

    @BeforeEach
    void configurar() {
        consumer = new PedidoCriadoConsumer(processarPagamento);
    }

    @Test
    @DisplayName("Deve consumir evento e processar pagamento")
    void t1() {
        Long pedidoId = 123L;
        BigDecimal valor = new BigDecimal("50.00");
        String cpf = "12345678901";
        PedidoCriadoEvent event = new PedidoCriadoEvent(pedidoId, valor, cpf);

        consumer.consumir(event);

        verify(processarPagamento, times(1)).executar(pedidoId, valor);
    }

    @Test
    @DisplayName("Deve consumir evento com valores corretos")
    void t2() {
        Long pedidoId = 456L;
        BigDecimal valor = new BigDecimal("100.00");
        String cpf = "98765432100";
        PedidoCriadoEvent event = new PedidoCriadoEvent(pedidoId, valor, cpf);

        consumer.consumir(event);

        verify(processarPagamento).executar(pedidoId, valor);
    }

    @Test
    @DisplayName("Deve chamar use case ao consumir evento")
    void t3() {
        Long pedidoId = 789L;
        BigDecimal valor = new BigDecimal("75.50");
        PedidoCriadoEvent event = new PedidoCriadoEvent(pedidoId, valor, null);

        consumer.consumir(event);

        verify(processarPagamento, times(1)).executar(anyLong(), any(BigDecimal.class));
    }
}
