package br.com.lanchonete.cozinha.adapters.messaging.consumer;

import br.com.lanchonete.cozinha.adapters.messaging.dto.PagamentoAprovadoEvent;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoAprovadoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoAprovadoConsumer.class);
    private final PedidoCozinhaRepository pedidoCozinhaRepository;

    public PagamentoAprovadoConsumer(PedidoCozinhaRepository pedidoCozinhaRepository) {
        this.pedidoCozinhaRepository = pedidoCozinhaRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pagamento-aprovado}")
    public void receberPagamentoAprovado(PagamentoAprovadoEvent event) {
        logger.info("Pagamento aprovado recebido para pedido ID: {}", event.getPedidoId());

        try {
            PedidoCozinha pedido = new PedidoCozinha(event.getPedidoId());
            pedidoCozinhaRepository.save(pedido);
            logger.info("Pedido {} adicionado na fila da cozinha", event.getPedidoId());
        } catch (Exception e) {
            logger.error("Erro ao adicionar pedido na fila da cozinha", e);
            throw e;
        }
    }
}
