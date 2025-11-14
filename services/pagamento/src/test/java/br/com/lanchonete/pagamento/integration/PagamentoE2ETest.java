package br.com.lanchonete.pagamento.integration;

import br.com.lanchonete.pagamento.adapters.messaging.PagamentoEvent;
import br.com.lanchonete.pagamento.adapters.messaging.PedidoCriadoEvent;
import br.com.lanchonete.pagamento.adapters.persistence.PagamentoDocument;
import br.com.lanchonete.pagamento.adapters.persistence.PagamentoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class PagamentoE2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    private ObjectMapper objectMapper;

    private static final String PEDIDO_EXCHANGE = "pedido.events";
    private static final String PAGAMENTO_EXCHANGE = "pagamento.events";
    private static final String PEDIDO_CRIADO_QUEUE = "pagamentos.pedido-criado";
    private static final String PAGAMENTO_APROVADO_QUEUE = "test.pagamento-aprovado";
    private static final String PAGAMENTO_REJEITADO_QUEUE = "test.pagamento-rejeitado";
    private static final String PEDIDO_CRIADO_KEY = "pedido.criado";
    private static final String PAGAMENTO_APROVADO_KEY = "pagamento.aprovado";
    private static final String PAGAMENTO_REJEITADO_KEY = "pagamento.rejeitado";

    @BeforeEach
    void configurar() {
        objectMapper = new ObjectMapper();

        // Criar filas temporárias para capturar eventos de pagamento
        // Filas não-duráveis, não-exclusivas, sem auto-delete
        Queue pagamentoAprovadoQueue = new Queue(PAGAMENTO_APROVADO_QUEUE, false, false, false);
        Queue pagamentoRejeitadoQueue = new Queue(PAGAMENTO_REJEITADO_QUEUE, false, false, false);

        rabbitAdmin.declareQueue(pagamentoAprovadoQueue);
        rabbitAdmin.declareQueue(pagamentoRejeitadoQueue);

        // Criar bindings para capturar eventos
        DirectExchange pagamentoExchange = new DirectExchange(PAGAMENTO_EXCHANGE);

        Binding pagamentoAprovadoBinding = BindingBuilder
                .bind(pagamentoAprovadoQueue)
                .to(pagamentoExchange)
                .with(PAGAMENTO_APROVADO_KEY);

        Binding pagamentoRejeitadoBinding = BindingBuilder
                .bind(pagamentoRejeitadoQueue)
                .to(pagamentoExchange)
                .with(PAGAMENTO_REJEITADO_KEY);

        rabbitAdmin.declareBinding(pagamentoAprovadoBinding);
        rabbitAdmin.declareBinding(pagamentoRejeitadoBinding);

        // Limpar dados anteriores
        pagamentoRepository.deleteAll();

        // Limpar filas (ignorar se ainda não existirem)
        try {
            rabbitAdmin.purgeQueue(PAGAMENTO_APROVADO_QUEUE, false);
            rabbitAdmin.purgeQueue(PAGAMENTO_REJEITADO_QUEUE, false);
        } catch (Exception e) {
            // Filas podem ainda não existir, ignorar
        }
    }

    @Test
    @DisplayName("E2E: Deve processar evento PedidoCriado e publicar PagamentoAprovado/Rejeitado")
    void t1() {
        // Given: Um evento de pedido criado
        Long pedidoId = 12345L;
        BigDecimal valor = new BigDecimal("150.00");
        String cpf = "12345678901";
        PedidoCriadoEvent evento = new PedidoCriadoEvent(pedidoId, valor, cpf);

        // When: Publicamos o evento na fila de pedidos criados
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, PEDIDO_CRIADO_KEY, evento);

        // Then: Aguardamos até que:
        // 1. O pagamento seja persistido no MongoDB
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = pagamentoRepository.count();
            assertTrue(count > 0, "Pagamento deveria ter sido persistido no MongoDB");
        });

        // 2. Um evento de pagamento seja publicado (aprovado OU rejeitado)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message aprovadoMsg = rabbitTemplate.receive(PAGAMENTO_APROVADO_QUEUE, 1000);
            Message rejeitadoMsg = rabbitTemplate.receive(PAGAMENTO_REJEITADO_QUEUE, 1000);

            assertTrue(aprovadoMsg != null || rejeitadoMsg != null,
                    "Deveria ter publicado evento de pagamento aprovado OU rejeitado");

            if (aprovadoMsg != null) {
                PagamentoEvent event = objectMapper.readValue(aprovadoMsg.getBody(), PagamentoEvent.class);
                assertEquals(pedidoId, event.pedidoId());
            }

            if (rejeitadoMsg != null) {
                PagamentoEvent event = objectMapper.readValue(rejeitadoMsg.getBody(), PagamentoEvent.class);
                assertEquals(pedidoId, event.pedidoId());
            }
        });

        // 3. Verificar que o pagamento tem o pedidoId correto
        PagamentoDocument pagamento = pagamentoRepository.findAll().get(0);
        assertEquals(pedidoId, pagamento.getPedidoId());
        assertEquals(valor, pagamento.getValor());
        assertNotNull(pagamento.getStatus());
        assertTrue(pagamento.getStatus().equals("APROVADO") || pagamento.getStatus().equals("REJEITADO"));
    }

    @Test
    @DisplayName("E2E: Deve processar múltiplos eventos de PedidoCriado")
    void t2() {
        // Given: Múltiplos eventos de pedidos criados
        PedidoCriadoEvent evento1 = new PedidoCriadoEvent(100L, new BigDecimal("10.00"), "111");
        PedidoCriadoEvent evento2 = new PedidoCriadoEvent(200L, new BigDecimal("20.00"), "222");
        PedidoCriadoEvent evento3 = new PedidoCriadoEvent(300L, new BigDecimal("30.00"), "333");

        // When: Publicamos os eventos
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, PEDIDO_CRIADO_KEY, evento1);
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, PEDIDO_CRIADO_KEY, evento2);
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, PEDIDO_CRIADO_KEY, evento3);

        // Then: Todos os pagamentos devem ser persistidos
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = pagamentoRepository.count();
            assertEquals(3, count, "Deveriam ter sido criados 3 pagamentos");
        });

        // E pelo menos um evento de pagamento deve ser publicado para cada
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            int totalEventos = 0;

            // Contar eventos aprovados
            Message msg;
            while ((msg = rabbitTemplate.receive(PAGAMENTO_APROVADO_QUEUE, 100)) != null) {
                totalEventos++;
            }

            // Contar eventos rejeitados
            while ((msg = rabbitTemplate.receive(PAGAMENTO_REJEITADO_QUEUE, 100)) != null) {
                totalEventos++;
            }

            assertTrue(totalEventos >= 3, "Deveriam ter sido publicados pelo menos 3 eventos de pagamento");
        });
    }

    @Test
    @DisplayName("E2E: Deve validar o formato do evento PagamentoAprovado publicado")
    void t3() throws Exception {
        // Given
        Long pedidoId = 999L;
        PedidoCriadoEvent evento = new PedidoCriadoEvent(pedidoId, new BigDecimal("99.99"), "999");

        // When
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, PEDIDO_CRIADO_KEY, evento);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message aprovadoMsg = rabbitTemplate.receive(PAGAMENTO_APROVADO_QUEUE, 1000);
            Message rejeitadoMsg = rabbitTemplate.receive(PAGAMENTO_REJEITADO_QUEUE, 1000);

            Message eventMessage = aprovadoMsg != null ? aprovadoMsg : rejeitadoMsg;
            assertNotNull(eventMessage, "Deveria ter recebido um evento");

            PagamentoEvent event = objectMapper.readValue(eventMessage.getBody(), PagamentoEvent.class);
            assertNotNull(event);
            assertNotNull(event.pedidoId());
            assertEquals(pedidoId, event.pedidoId());
        });
    }

    @Test
    @DisplayName("E2E: Deve validar que containers estão rodando")
    void t4() {
        assertTrue(mongoDBContainer.isRunning());
        assertTrue(rabbitMQContainer.isRunning());
        assertNotNull(rabbitTemplate);
        assertNotNull(pagamentoRepository);
    }

    @Test
    @DisplayName("E2E: Deve persistir pagamento com dataCriacao")
    void t5() {
        // Given
        Long pedidoId = 777L;
        PedidoCriadoEvent evento = new PedidoCriadoEvent(pedidoId, new BigDecimal("77.77"), "777");

        // When
        rabbitTemplate.convertAndSend(PEDIDO_EXCHANGE, PEDIDO_CRIADO_KEY, evento);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = pagamentoRepository.count();
            assertTrue(count > 0);

            PagamentoDocument pagamento = pagamentoRepository.findAll().get(0);
            assertNotNull(pagamento.getDataCriacao());
            assertEquals(pedidoId, pagamento.getPedidoId());
        });
    }
}
