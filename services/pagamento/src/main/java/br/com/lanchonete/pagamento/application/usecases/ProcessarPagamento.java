package br.com.lanchonete.pagamento.application.usecases;

import br.com.lanchonete.pagamento.application.gateways.EventPublisher;
import br.com.lanchonete.pagamento.application.gateways.PagamentoGateway;
import br.com.lanchonete.pagamento.domain.model.Pagamento;
import br.com.lanchonete.pagamento.domain.model.StatusPagamento;
import br.com.lanchonete.pagamento.domain.model.Valor;

import java.math.BigDecimal;

public class ProcessarPagamento {

    private static final double PERCENTUAL_APROVACAO = 0.8; // 80%

    private final PagamentoGateway pagamentoGateway;
    private final EventPublisher eventPublisher;

    public ProcessarPagamento(PagamentoGateway pagamentoGateway, EventPublisher eventPublisher) {
        this.pagamentoGateway = pagamentoGateway;
        this.eventPublisher = eventPublisher;
    }

    public Pagamento executar(Long pedidoId, BigDecimal valorPagamento) {
        // Mock: 80% aprovação
        StatusPagamento status = Math.random() < PERCENTUAL_APROVACAO
                ? StatusPagamento.APROVADO
                : StatusPagamento.REJEITADO;

        // Criar pagamento
        Valor valor = new Valor(valorPagamento);
        Pagamento pagamento = new Pagamento(pedidoId, valor, status);

        // Persistir no MongoDB
        Pagamento pagamentoSalvo = pagamentoGateway.salvar(pagamento);

        // Publicar evento correspondente
        if (status.isAprovado()) {
            eventPublisher.publicarPagamentoAprovado(pedidoId);
        } else {
            eventPublisher.publicarPagamentoRejeitado(pedidoId);
        }

        return pagamentoSalvo;
    }
}
