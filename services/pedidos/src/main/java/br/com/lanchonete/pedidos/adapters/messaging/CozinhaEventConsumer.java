package br.com.lanchonete.pedidos.adapters.messaging;

import br.com.lanchonete.pedidos.adapters.messaging.events.PedidoProntoEvent;
import br.com.lanchonete.pedidos.application.usecases.AtualizarStatusPedidoUseCase;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CozinhaEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CozinhaEventConsumer.class);

    private final AtualizarStatusPedidoUseCase atualizarStatusUseCase;

    public CozinhaEventConsumer(AtualizarStatusPedidoUseCase atualizarStatusUseCase) {
        this.atualizarStatusUseCase = atualizarStatusUseCase;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pedido-pronto}")
    public void onPedidoPronto(PedidoProntoEvent event) {
        logger.info("Recebido evento PedidoPronto para pedido: {}", event.getPedidoId());

        try {
            atualizarStatusUseCase.executar(event.getPedidoId(), StatusPedido.PRONTO);
            logger.info("Pedido {} atualizado para PRONTO", event.getPedidoId());
        } catch (Exception e) {
            logger.error("Erro ao processar PedidoPronto: {}", e.getMessage(), e);
        }
    }
}
