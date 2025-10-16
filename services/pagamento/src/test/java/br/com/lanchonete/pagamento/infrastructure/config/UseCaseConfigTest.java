package br.com.lanchonete.pagamento.infrastructure.config;

import br.com.lanchonete.pagamento.adapters.persistence.PagamentoRepository;
import br.com.lanchonete.pagamento.application.gateways.EventPublisher;
import br.com.lanchonete.pagamento.application.gateways.PagamentoGateway;
import br.com.lanchonete.pagamento.application.usecases.ProcessarPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UseCaseConfig.class})
class UseCaseConfigTest {

    @MockBean
    private PagamentoRepository pagamentoRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PagamentoGateway pagamentoGateway;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private ProcessarPagamento processarPagamento;

    @Test
    @DisplayName("Deve criar os beans dos UCs de pagamento")
    void t1() {
        assertNotNull(pagamentoGateway);
        assertNotNull(eventPublisher);
        assertNotNull(processarPagamento);
    }
}
