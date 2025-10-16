package br.com.lanchonete.pagamento.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.pedido}")
    private String pedidoExchange;

    @Value("${rabbitmq.exchange.pagamento}")
    private String pagamentoExchange;

    @Value("${rabbitmq.queue.pedido-criado}")
    private String pedidoCriadoQueue;

    @Value("${rabbitmq.routing-key.pedido-criado}")
    private String pedidoCriadoKey;

    @Bean
    DirectExchange pedidoExchange() {
        return new DirectExchange(pedidoExchange);
    }

    @Bean
    DirectExchange pagamentoExchange() {
        return new DirectExchange(pagamentoExchange);
    }

    @Bean
    Queue pedidoCriadoQueue() {
        return new Queue(pedidoCriadoQueue, true);
    }

    @Bean
    Binding pedidoCriadoBinding(Queue pedidoCriadoQueue, DirectExchange pedidoExchange) {
        return BindingBuilder.bind(pedidoCriadoQueue)
                .to(pedidoExchange)
                .with(pedidoCriadoKey);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
