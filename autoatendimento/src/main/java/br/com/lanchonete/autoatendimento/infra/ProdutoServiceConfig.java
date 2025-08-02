package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.aplicacao.gateways.ProdutoGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoServiceConfig {

    @Bean
    BuscarProdutosPorCategoria buscarProdutosPorCategoria(final ProdutoGateway produtoGateway) {
        return new BuscarProdutosPorCategoria(produtoGateway);
    }

    @Bean
    CriarProduto criarProduto(final ProdutoGateway produtoGateway) {
        return new CriarProduto(produtoGateway);
    }

    @Bean
    EditarProduto editarProduto(final ProdutoGateway produtoGateway) {
        return new EditarProduto(produtoGateway);
    }

    @Bean
    public RemoverProduto removerProduto(final ProdutoGateway produtoGateway) {
        return new RemoverProduto(produtoGateway);
    }
}
