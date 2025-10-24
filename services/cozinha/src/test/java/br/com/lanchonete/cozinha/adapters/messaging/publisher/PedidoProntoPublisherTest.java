package br.com.lanchonete.cozinha.adapters.messaging.publisher;

import br.com.lanchonete.cozinha.adapters.messaging.dto.PedidoProntoEvent;
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
class PedidoProntoPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private PedidoProntoPublisher pedidoProntoPublisher;

    @BeforeEach
    void configurar() {
        pedidoProntoPublisher = new PedidoProntoPublisher(rabbitTemplate, "pedidos.events", "pedido.pronto");
    }

    @Test
    @DisplayName("Deve publicar evento de pedido pronto")
    void t1() {
        Long pedidoId = 123L;

        pedidoProntoPublisher.publicar(pedidoId);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), any(PedidoProntoEvent.class));
    }

    @Test
    @DisplayName("Deve publicar evento com pedidoId correto")
    void t2() {
        Long pedidoId = 789L;

        pedidoProntoPublisher.publicar(pedidoId);

        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                argThat((Object event) -> ((PedidoProntoEvent) event).getPedidoId().equals(pedidoId))
        );
    }
}
