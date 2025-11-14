package br.com.lanchonete.pedidos.application.gateways;

public interface ClienteGateway {
    boolean existeCliente(String cpf);
    String buscarNomeCliente(String cpf);
}
