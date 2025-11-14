package br.com.lanchonete.pagamento.integration;

import br.com.lanchonete.pagamento.adapters.messaging.PagamentoEvent;
import br.com.lanchonete.pagamento.adapters.messaging.PedidoCriadoEvent;
import br.com.lanchonete.pagamento.adapters.messaging.RabbitEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RabbitMQIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    private RabbitTemplate rabbitTemplate;
    private RabbitAdmin rabbitAdmin;
    private RabbitEventPublisher eventPublisher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void configurar() {
        // Configurar connection factory
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMQContainer.getHost());
        connectionFactory.setPort(rabbitMQContainer.getAmqpPort());
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        // Configurar RabbitTemplate
        rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        // Configurar RabbitAdmin
        rabbitAdmin = new RabbitAdmin(connectionFactory);

        // Criar exchanges
        DirectExchange pedidoExchange = new DirectExchange("pedido.events");
        DirectExchange pagamentoExchange = new DirectExchange("pagamento.events");
        rabbitAdmin.declareExchange(pedidoExchange);
        rabbitAdmin.declareExchange(pagamentoExchange);

        // Criar filas
        Queue pedidoCriadoQueue = new Queue("pagamentos.pedido-criado", true);
        rabbitAdmin.declareQueue(pedidoCriadoQueue);

        // Criar bindings
        Binding binding = BindingBuilder
                .bind(pedidoCriadoQueue)
                .to(pedidoExchange)
                .with("pedido.criado");
        rabbitAdmin.declareBinding(binding);

        // Criar event publisher
        eventPublisher = new RabbitEventPublisher(rabbitTemplate);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve conectar com RabbitMQ via TestContainers")
    void t1() {
        assertTrue(rabbitMQContainer.isRunning());
        assertNotNull(rabbitTemplate);
        assertNotNull(rabbitAdmin);
    }

    @Test
    @DisplayName("Deve publicar evento de pagamento aprovado")
    void t2() {
        Long pedidoId = 123L;

        assertDoesNotThrow(() -> {
            eventPublisher.publicarPagamentoAprovado(pedidoId);
        });
    }

    @Test
    @DisplayName("Deve publicar evento de pagamento rejeitado")
    void t3() {
        Long pedidoId = 456L;

        assertDoesNotThrow(() -> {
            eventPublisher.publicarPagamentoRejeitado(pedidoId);
        });
    }

    @Test
    @DisplayName("Deve serializar evento de pagamento corretamente")
    void t4() throws Exception {
        Long pedidoId = 789L;
        PagamentoEvent event = new PagamentoEvent(pedidoId);

        String json = objectMapper.writeValueAsString(event);

        assertNotNull(json);
        assertTrue(json.contains("789"));
        assertTrue(json.contains("pedidoId"));
    }

    @Test
    @DisplayName("Deve deserializar evento de pedido criado")
    void t5() throws Exception {
        Long pedidoId = 999L;
        BigDecimal valor = new BigDecimal("50.00");
        String cpf = "12345678901";

        String json = String.format("{\"pedidoId\":%d,\"valor\":%s,\"cpf\":\"%s\"}", pedidoId, valor, cpf);
        PedidoCriadoEvent event = objectMapper.readValue(json, PedidoCriadoEvent.class);

        assertNotNull(event);
        assertEquals(pedidoId, event.pedidoId());
        assertEquals(valor, event.valor());
        assertEquals(cpf, event.cpf());
    }

    @Test
    @DisplayName("Deve publicar múltiplos eventos sem erro")
    void t6() {
        assertDoesNotThrow(() -> {
            eventPublisher.publicarPagamentoAprovado(100L);
            eventPublisher.publicarPagamentoRejeitado(200L);
            eventPublisher.publicarPagamentoAprovado(300L);
            eventPublisher.publicarPagamentoRejeitado(400L);
        });
    }

    @Test
    @DisplayName("Deve publicar evento e verificar conexão funciona")
    void t7() {
        Long pedidoId = 555L;

        eventPublisher.publicarPagamentoAprovado(pedidoId);

        // Verificar que a conexão funciona
        assertDoesNotThrow(() -> rabbitTemplate.getConnectionFactory().createConnection());
    }

    @Test
    @DisplayName("Deve publicar evento para exchange pagamento.events com routing key correta")
    void t8() {
        Long pedidoId = 777L;

        // Criar uma fila temporária para capturar mensagens
        Queue tempQueue = new Queue("temp-queue-aprovado", false, true, true);
        rabbitAdmin.declareQueue(tempQueue);

        DirectExchange pagamentoExchange = new DirectExchange("pagamento.events");
        Binding binding = BindingBuilder
                .bind(tempQueue)
                .to(pagamentoExchange)
                .with("pagamento.aprovado");
        rabbitAdmin.declareBinding(binding);

        // Publicar evento
        eventPublisher.publicarPagamentoAprovado(pedidoId);

        // Tentar receber mensagem
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive("temp-queue-aprovado", 1000);
            assertNotNull(message, "Mensagem deveria ter sido recebida");
        });
    }

    @Test
    @DisplayName("Deve publicar evento rejeitado com routing key correta")
    void t9() {
        Long pedidoId = 888L;

        // Criar uma fila temporária para capturar mensagens
        Queue tempQueue = new Queue("temp-queue-rejeitado", false, true, true);
        rabbitAdmin.declareQueue(tempQueue);

        DirectExchange pagamentoExchange = new DirectExchange("pagamento.events");
        Binding binding = BindingBuilder
                .bind(tempQueue)
                .to(pagamentoExchange)
                .with("pagamento.rejeitado");
        rabbitAdmin.declareBinding(binding);

        // Publicar evento
        eventPublisher.publicarPagamentoRejeitado(pedidoId);

        // Tentar receber mensagem
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive("temp-queue-rejeitado", 1000);
            assertNotNull(message, "Mensagem deveria ter sido recebida");
        });
    }

    @Test
    @DisplayName("Deve validar que container RabbitMQ está expondo portas corretas")
    void t10() {
        Integer amqpPort = rabbitMQContainer.getAmqpPort();
        Integer managementPort = rabbitMQContainer.getHttpPort();

        assertNotNull(amqpPort);
        assertNotNull(managementPort);
        assertTrue(amqpPort > 0);
        assertTrue(managementPort > 0);
    }
}
