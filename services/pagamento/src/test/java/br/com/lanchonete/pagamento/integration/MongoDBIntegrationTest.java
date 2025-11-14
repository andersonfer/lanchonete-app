package br.com.lanchonete.pagamento.integration;

import br.com.lanchonete.pagamento.adapters.persistence.PagamentoDocument;
import br.com.lanchonete.pagamento.adapters.persistence.PagamentoGatewayMongo;
import br.com.lanchonete.pagamento.adapters.persistence.PagamentoRepository;
import br.com.lanchonete.pagamento.domain.model.Pagamento;
import br.com.lanchonete.pagamento.domain.model.StatusPagamento;
import br.com.lanchonete.pagamento.domain.model.Valor;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MongoDBIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    private PagamentoRepository repository;
    private PagamentoGatewayMongo gateway;
    private MongoClient mongoClient;
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void configurar() {
        // Configurar conexão com MongoDB
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        mongoClient = MongoClients.create(connectionString);

        // Configurar MongoTemplate
        mongoTemplate = new MongoTemplate(mongoClient, "test");

        // Criar repository usando MongoRepositoryFactory
        MongoRepositoryFactory factory = new MongoRepositoryFactory(mongoTemplate);
        repository = factory.getRepository(PagamentoRepository.class);

        // Limpar dados anteriores
        mongoTemplate.dropCollection(PagamentoDocument.class);

        // Criar gateway
        gateway = new PagamentoGatewayMongo(repository);
    }

    @Test
    @DisplayName("Deve conectar com MongoDB via TestContainers")
    void t1() {
        assertTrue(mongoDBContainer.isRunning());
        assertNotNull(mongoClient);
        assertNotNull(mongoTemplate);
        assertNotNull(repository);
    }

    @Test
    @DisplayName("Deve salvar pagamento no MongoDB")
    void t2() {
        Long pedidoId = 123L;
        Valor valor = new Valor(new BigDecimal("50.00"));
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.APROVADO);

        Pagamento salvo = gateway.salvar(pagamento);

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals(pedidoId, salvo.getPedidoId());
        assertEquals(valor.getValor(), salvo.getValor());
        assertEquals(StatusPagamento.APROVADO, salvo.getStatus());
        assertNotNull(salvo.getDataCriacao());
    }

    @Test
    @DisplayName("Deve persistir pagamento com status APROVADO")
    void t3() {
        Long pedidoId = 456L;
        Valor valor = new Valor(new BigDecimal("100.00"));
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.APROVADO);

        Pagamento salvo = gateway.salvar(pagamento);

        assertNotNull(salvo.getId());
        assertEquals(pedidoId, salvo.getPedidoId());
        assertEquals(StatusPagamento.APROVADO, salvo.getStatus());
    }

    @Test
    @DisplayName("Deve persistir pagamento com status REJEITADO")
    void t4() {
        Long pedidoId = 789L;
        Valor valor = new Valor(new BigDecimal("75.50"));
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.REJEITADO);

        Pagamento salvo = gateway.salvar(pagamento);

        assertNotNull(salvo.getId());
        assertEquals(StatusPagamento.REJEITADO, salvo.getStatus());
    }

    @Test
    @DisplayName("Deve salvar múltiplos pagamentos")
    void t5() {
        Pagamento p1 = new Pagamento(111L, new Valor(new BigDecimal("10.00")), StatusPagamento.APROVADO);
        Pagamento p2 = new Pagamento(222L, new Valor(new BigDecimal("20.00")), StatusPagamento.REJEITADO);
        Pagamento p3 = new Pagamento(333L, new Valor(new BigDecimal("30.00")), StatusPagamento.APROVADO);

        gateway.salvar(p1);
        gateway.salvar(p2);
        gateway.salvar(p3);

        long count = repository.count();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Deve persistir e recuperar dados corretamente via repository")
    void t6() {
        Long pedidoId = 999L;
        BigDecimal valorDecimal = new BigDecimal("123.45");
        Valor valor = new Valor(valorDecimal);
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.APROVADO);

        Pagamento salvo = gateway.salvar(pagamento);

        Optional<PagamentoDocument> document = repository.findById(salvo.getId());
        assertTrue(document.isPresent());
        assertEquals(valorDecimal, document.get().getValor());
        assertNotNull(document.get().getDataCriacao());
    }

    @Test
    @DisplayName("Deve converter corretamente entre Document e Domain")
    void t7() {
        Long pedidoId = 888L;
        BigDecimal valorDecimal = new BigDecimal("99.99");
        Valor valor = new Valor(valorDecimal);
        StatusPagamento status = StatusPagamento.APROVADO;
        Pagamento pagamento = new Pagamento(pedidoId, valor, status);

        Pagamento salvo = gateway.salvar(pagamento);

        Optional<PagamentoDocument> document = repository.findById(salvo.getId());
        assertTrue(document.isPresent());
        assertEquals(pedidoId, document.get().getPedidoId());
        assertEquals(valorDecimal, document.get().getValor());
        assertEquals(status.name(), document.get().getStatus());
        assertNotNull(document.get().getDataCriacao());
    }

    @Test
    @DisplayName("Deve salvar pagamentos com valores diferentes")
    void t8() {
        Pagamento p1 = new Pagamento(100L, new Valor(new BigDecimal("0.01")), StatusPagamento.APROVADO);
        Pagamento p2 = new Pagamento(200L, new Valor(new BigDecimal("999999.99")), StatusPagamento.APROVADO);

        Pagamento salvo1 = gateway.salvar(p1);
        Pagamento salvo2 = gateway.salvar(p2);

        assertNotNull(salvo1.getId());
        assertNotNull(salvo2.getId());
        assertNotEquals(salvo1.getId(), salvo2.getId());
    }

    @Test
    @DisplayName("Deve buscar pagamento por ID usando repository")
    void t9() {
        Long pedidoId = 555L;
        Valor valor = new Valor(new BigDecimal("50.00"));
        Pagamento pagamento = new Pagamento(pedidoId, valor, StatusPagamento.APROVADO);

        Pagamento salvo = gateway.salvar(pagamento);
        Optional<PagamentoDocument> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(salvo.getId(), encontrado.get().getId());
        assertEquals(pedidoId, encontrado.get().getPedidoId());
    }

    @Test
    @DisplayName("Deve validar que container MongoDB está expondo porta correta")
    void t10() {
        Integer mongoPort = mongoDBContainer.getFirstMappedPort();
        String replicaSetUrl = mongoDBContainer.getReplicaSetUrl();

        assertNotNull(mongoPort);
        assertTrue(mongoPort > 0);
        assertNotNull(replicaSetUrl);
        assertTrue(replicaSetUrl.contains("mongodb://"));
    }
}
