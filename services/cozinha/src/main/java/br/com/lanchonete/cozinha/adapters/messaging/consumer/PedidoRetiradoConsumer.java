package br.com.lanchonete.cozinha.adapters.messaging.consumer;

import br.com.lanchonete.cozinha.adapters.messaging.dto.PedidoRetiradoEvent;
import br.com.lanchonete.cozinha.domain.model.PedidoCozinha;
import br.com.lanchonete.cozinha.domain.repository.PedidoCozinhaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoRetiradoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PedidoRetiradoConsumer.class);
    private final PedidoCozinhaRepository pedidoCozinhaRepository;

    public PedidoRetiradoConsumer(PedidoCozinhaRepository pedidoCozinhaRepository) {
        this.pedidoCozinhaRepository = pedidoCozinhaRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pedido-retirado}")
    public void receberPedidoRetirado(PedidoRetiradoEvent event) {
        logger.info("Pedido retirado recebido para pedido ID: {}", event.getPedidoId());

        try {
            PedidoCozinha pedido = pedidoCozinhaRepository.findByPedidoId(event.getPedidoId())
                    .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado na fila da cozinha"));

            pedido.marcarComoRetirado();
            pedidoCozinhaRepository.save(pedido);
            logger.info("Pedido {} marcado como retirado na fila da cozinha", event.getPedidoId());
        } catch (Exception e) {
            logger.error("Erro ao marcar pedido como retirado", e);
            throw e;
        }
    }
}
