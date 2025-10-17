package br.com.lanchonete.pedidos.adapters.messaging;

import br.com.lanchonete.pedidos.adapters.messaging.events.PedidoCriadoEvent;
import br.com.lanchonete.pedidos.adapters.messaging.events.PedidoRetiradoEvent;
import br.com.lanchonete.pedidos.application.gateways.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pedido}")
    private String pedidoExchange;

    @Value("${rabbitmq.routingkey.pedido-criado}")
    private String pedidoCriadoRoutingKey;

    @Value("${rabbitmq.routingkey.pedido-retirado}")
    private String pedidoRetiradoRoutingKey;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarPedidoCriado(Long pedidoId, String valorTotal, String cpfCliente) {
        PedidoCriadoEvent event = new PedidoCriadoEvent(pedidoId, valorTotal, cpfCliente);
        rabbitTemplate.convertAndSend(pedidoExchange, pedidoCriadoRoutingKey, event);
    }

    @Override
    public void publicarPedidoRetirado(Long pedidoId) {
        PedidoRetiradoEvent event = new PedidoRetiradoEvent(pedidoId);
        rabbitTemplate.convertAndSend(pedidoExchange, pedidoRetiradoRoutingKey, event);
    }
}
