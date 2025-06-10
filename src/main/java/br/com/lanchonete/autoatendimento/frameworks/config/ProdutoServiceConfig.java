package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
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
