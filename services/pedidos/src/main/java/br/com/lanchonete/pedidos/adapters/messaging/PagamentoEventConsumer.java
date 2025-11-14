package br.com.lanchonete.pedidos.adapters.messaging;

import br.com.lanchonete.pedidos.adapters.messaging.events.PagamentoAprovadoEvent;
import br.com.lanchonete.pedidos.adapters.messaging.events.PagamentoRejeitadoEvent;
import br.com.lanchonete.pedidos.application.usecases.AtualizarStatusPedidoUseCase;
import br.com.lanchonete.pedidos.domain.model.StatusPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoEventConsumer.class);

    private final AtualizarStatusPedidoUseCase atualizarStatusUseCase;

    public PagamentoEventConsumer(AtualizarStatusPedidoUseCase atualizarStatusUseCase) {
        this.atualizarStatusUseCase = atualizarStatusUseCase;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pagamento-aprovado}")
    public void onPagamentoAprovado(PagamentoAprovadoEvent event) {
        logger.info("Recebido evento PagamentoAprovado para pedido: {}", event.getPedidoId());

        try {
            atualizarStatusUseCase.executar(event.getPedidoId(), StatusPedido.REALIZADO);
            logger.info("Pedido {} atualizado para REALIZADO", event.getPedidoId());
        } catch (Exception e) {
            logger.error("Erro ao processar PagamentoAprovado: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.pagamento-rejeitado}")
    public void onPagamentoRejeitado(PagamentoRejeitadoEvent event) {
        logger.info("Recebido evento PagamentoRejeitado para pedido: {}", event.getPedidoId());

        try {
            atualizarStatusUseCase.executar(event.getPedidoId(), StatusPedido.CANCELADO);
            logger.info("Pedido {} atualizado para CANCELADO", event.getPedidoId());
        } catch (Exception e) {
            logger.error("Erro ao processar PagamentoRejeitado: {}", e.getMessage(), e);
        }
    }
}
