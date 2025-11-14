package br.com.lanchonete.pagamento.adapters.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitEventPublisher eventPublisher;

    @BeforeEach
    void configurar() {
        eventPublisher = new RabbitEventPublisher(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve publicar evento de pagamento aprovado")
    void t1() {
        Long pedidoId = 123L;

        eventPublisher.publicarPagamentoAprovado(pedidoId);

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("pagamento.events"), eq("pagamento.aprovado"), any(PagamentoEvent.class));
    }

    @Test
    @DisplayName("Deve publicar evento de pagamento rejeitado")
    void t2() {
        Long pedidoId = 456L;

        eventPublisher.publicarPagamentoRejeitado(pedidoId);

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("pagamento.events"), eq("pagamento.rejeitado"), any(PagamentoEvent.class));
    }

    @Test
    @DisplayName("Deve publicar evento com pedidoId correto")
    void t3() {
        Long pedidoId = 789L;

        eventPublisher.publicarPagamentoAprovado(pedidoId);

        verify(rabbitTemplate).convertAndSend(
                eq("pagamento.events"),
                eq("pagamento.aprovado"),
                argThat((Object event) -> ((PagamentoEvent) event).pedidoId().equals(pedidoId))
        );
    }
}
