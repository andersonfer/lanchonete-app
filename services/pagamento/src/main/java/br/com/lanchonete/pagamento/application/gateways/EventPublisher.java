package br.com.lanchonete.pagamento.application.gateways;

public interface EventPublisher {

    void publicarPagamentoAprovado(Long pedidoId);

    void publicarPagamentoRejeitado(Long pedidoId);
}
