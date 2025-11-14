package br.com.lanchonete.pagamento.adapters.messaging;

import br.com.lanchonete.pagamento.application.gateways.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitEventPublisher implements EventPublisher {

    private static final String PAGAMENTO_EXCHANGE = "pagamento.events";
    private static final String PAGAMENTO_APROVADO_KEY = "pagamento.aprovado";
    private static final String PAGAMENTO_REJEITADO_KEY = "pagamento.rejeitado";

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarPagamentoAprovado(Long pedidoId) {
        PagamentoEvent event = new PagamentoEvent(pedidoId);
        rabbitTemplate.convertAndSend(PAGAMENTO_EXCHANGE, PAGAMENTO_APROVADO_KEY, event);
    }

    @Override
    public void publicarPagamentoRejeitado(Long pedidoId) {
        PagamentoEvent event = new PagamentoEvent(pedidoId);
        rabbitTemplate.convertAndSend(PAGAMENTO_EXCHANGE, PAGAMENTO_REJEITADO_KEY, event);
    }
}
