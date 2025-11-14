package br.com.lanchonete.cozinha.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.cozinha}")
    private String exchangeCozinha;

    @Value("${rabbitmq.exchange.pedidos}")
    private String exchangePedidos;

    @Value("${rabbitmq.exchange.pagamento}")
    private String exchangePagamento;

    @Value("${rabbitmq.queue.pagamento-aprovado}")
    private String queuePagamentoAprovado;

    @Value("${rabbitmq.queue.pedido-retirado}")
    private String queuePedidoRetirado;

    @Bean
    public DirectExchange cozinhaExchange() {
        return new DirectExchange(exchangeCozinha);
    }

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(exchangePedidos);
    }

    @Bean
    public DirectExchange pagamentoExchange() {
        return new DirectExchange(exchangePagamento);
    }

    @Bean
    public Queue pagamentoAprovadoQueue() {
        return QueueBuilder.durable(queuePagamentoAprovado).build();
    }

    @Bean
    public Queue pedidoRetiradoQueue() {
        return QueueBuilder.durable(queuePedidoRetirado).build();
    }

    @Bean
    public Binding pagamentoAprovadoBinding() {
        return BindingBuilder
                .bind(pagamentoAprovadoQueue())
                .to(pagamentoExchange())
                .with("pagamento.aprovado");
    }

    @Bean
    public Binding pedidoRetiradoBinding() {
        return BindingBuilder
                .bind(pedidoRetiradoQueue())
                .to(pedidosExchange())
                .with("pedido.retirado");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
