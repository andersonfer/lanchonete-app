package br.com.lanchonete.pagamento.infrastructure.config;

import br.com.lanchonete.pagamento.adapters.messaging.RabbitEventPublisher;
import br.com.lanchonete.pagamento.adapters.persistence.PagamentoGatewayMongo;
import br.com.lanchonete.pagamento.adapters.persistence.PagamentoRepository;
import br.com.lanchonete.pagamento.application.gateways.EventPublisher;
import br.com.lanchonete.pagamento.application.gateways.PagamentoGateway;
import br.com.lanchonete.pagamento.application.usecases.ProcessarPagamento;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    PagamentoGateway pagamentoGateway(PagamentoRepository repository) {
        return new PagamentoGatewayMongo(repository);
    }

    @Bean
    EventPublisher eventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisher(rabbitTemplate);
    }

    @Bean
    ProcessarPagamento processarPagamento(PagamentoGateway pagamentoGateway, EventPublisher eventPublisher) {
        return new ProcessarPagamento(pagamentoGateway, eventPublisher);
    }
}
