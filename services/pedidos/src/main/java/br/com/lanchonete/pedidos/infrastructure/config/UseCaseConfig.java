package br.com.lanchonete.pedidos.infrastructure.config;

import br.com.lanchonete.pedidos.application.gateways.ClienteGateway;
import br.com.lanchonete.pedidos.application.gateways.EventPublisher;
import br.com.lanchonete.pedidos.application.gateways.PedidoGateway;
import br.com.lanchonete.pedidos.application.gateways.ProdutoGateway;
import br.com.lanchonete.pedidos.application.usecases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CriarPedidoUseCase criarPedidoUseCase(PedidoGateway pedidoGateway,
                                                  ProdutoGateway produtoGateway,
                                                  ClienteGateway clienteGateway,
                                                  EventPublisher eventPublisher) {
        return new CriarPedidoUseCase(pedidoGateway, produtoGateway, clienteGateway, eventPublisher);
    }

    @Bean
    public BuscarPedidoUseCase buscarPedidoUseCase(PedidoGateway pedidoGateway) {
        return new BuscarPedidoUseCase(pedidoGateway);
    }

    @Bean
    public ListarPedidosUseCase listarPedidosUseCase(PedidoGateway pedidoGateway) {
        return new ListarPedidosUseCase(pedidoGateway);
    }

    @Bean
    public RetirarPedidoUseCase retirarPedidoUseCase(PedidoGateway pedidoGateway, EventPublisher eventPublisher) {
        return new RetirarPedidoUseCase(pedidoGateway, eventPublisher);
    }

    @Bean
    public ListarProdutosUseCase listarProdutosUseCase(ProdutoGateway produtoGateway) {
        return new ListarProdutosUseCase(produtoGateway);
    }

    @Bean
    public AtualizarStatusPedidoUseCase atualizarStatusPedidoUseCase(PedidoGateway pedidoGateway) {
        return new AtualizarStatusPedidoUseCase(pedidoGateway);
    }
}
