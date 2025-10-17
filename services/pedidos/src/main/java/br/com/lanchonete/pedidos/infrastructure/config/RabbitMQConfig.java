package br.com.lanchonete.pedidos.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.pedido}")
    private String pedidoExchange;

    @Value("${rabbitmq.exchange.pagamento}")
    private String pagamentoExchange;

    @Value("${rabbitmq.exchange.cozinha}")
    private String cozinhaExchange;

    @Value("${rabbitmq.queue.pagamento-aprovado}")
    private String pagamentoAprovadoQueue;

    @Value("${rabbitmq.queue.pagamento-rejeitado}")
    private String pagamentoRejeitadoQueue;

    @Value("${rabbitmq.queue.pedido-pronto}")
    private String pedidoProntoQueue;

    @Value("${rabbitmq.routingkey.pagamento-aprovado}")
    private String pagamentoAprovadoRoutingKey;

    @Value("${rabbitmq.routingkey.pagamento-rejeitado}")
    private String pagamentoRejeitadoRoutingKey;

    @Value("${rabbitmq.routingkey.pedido-pronto}")
    private String pedidoProntoRoutingKey;

    // Exchanges
    @Bean
    public DirectExchange pedidoExchange() {
        return new DirectExchange(pedidoExchange);
    }

    @Bean
    public DirectExchange pagamentoExchange() {
        return new DirectExchange(pagamentoExchange);
    }

    @Bean
    public DirectExchange cozinhaExchange() {
        return new DirectExchange(cozinhaExchange);
    }

    // Queues
    @Bean
    public Queue pagamentoAprovadoQueue() {
        return new Queue(pagamentoAprovadoQueue, true);
    }

    @Bean
    public Queue pagamentoRejeitadoQueue() {
        return new Queue(pagamentoRejeitadoQueue, true);
    }

    @Bean
    public Queue pedidoProntoQueue() {
        return new Queue(pedidoProntoQueue, true);
    }

    // Bindings
    @Bean
    public Binding pagamentoAprovadoBinding() {
        return BindingBuilder
                .bind(pagamentoAprovadoQueue())
                .to(pagamentoExchange())
                .with(pagamentoAprovadoRoutingKey);
    }

    @Bean
    public Binding pagamentoRejeitadoBinding() {
        return BindingBuilder
                .bind(pagamentoRejeitadoQueue())
                .to(pagamentoExchange())
                .with(pagamentoRejeitadoRoutingKey);
    }

    @Bean
    public Binding pedidoProntoBinding() {
        return BindingBuilder
                .bind(pedidoProntoQueue())
                .to(cozinhaExchange())
                .with(pedidoProntoRoutingKey);
    }

    // Message Converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
