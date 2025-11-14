package br.com.lanchonete.cozinha.adapters.messaging.publisher;

import br.com.lanchonete.cozinha.adapters.messaging.dto.PedidoProntoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PedidoProntoPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PedidoProntoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PedidoProntoPublisher(RabbitTemplate rabbitTemplate,
                                  @Value("${rabbitmq.exchange.cozinha}") String exchange,
                                  @Value("${rabbitmq.routingkey.pedido-pronto}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicar(Long pedidoId) {
        PedidoProntoEvent event = new PedidoProntoEvent(pedidoId);
        logger.info("Publicando evento PedidoPronto para pedido ID: {}", pedidoId);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            logger.info("Evento PedidoPronto publicado com sucesso para pedido ID: {}", pedidoId);
        } catch (Exception e) {
            logger.error("Erro ao publicar evento PedidoPronto", e);
            throw e;
        }
    }
}
