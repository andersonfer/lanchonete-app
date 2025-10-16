package br.com.lanchonete.pagamento.adapters.messaging;

import br.com.lanchonete.pagamento.application.usecases.ProcessarPagamento;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoCriadoConsumer {

    private final ProcessarPagamento processarPagamento;

    public PedidoCriadoConsumer(ProcessarPagamento processarPagamento) {
        this.processarPagamento = processarPagamento;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pedido-criado}")
    public void consumir(PedidoCriadoEvent event) {
        processarPagamento.executar(event.pedidoId(), event.valor());
    }
}
