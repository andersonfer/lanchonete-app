package br.com.lanchonete.pedidos.application.gateways;

public interface EventPublisher {
    void publicarPedidoCriado(Long pedidoId, String valorTotal, String cpfCliente);
    void publicarPedidoRetirado(Long pedidoId);
}
