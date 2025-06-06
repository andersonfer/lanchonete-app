package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoServiceConfig {

    @Bean
    BuscarProdutosPorCategoria buscarProdutosPorCategoria(final ProdutoRepositorio produtoRepositorio) {
        return new BuscarProdutosPorCategoria(produtoRepositorio);
    }

    @Bean
    CriarProduto criarProduto(final ProdutoRepositorio produtoRepositorio) {
        return new CriarProduto(produtoRepositorio);
    }

    @Bean
    EditarProduto editarProduto(final ProdutoRepositorio produtoRepositorio) {
        return new EditarProduto(produtoRepositorio);
    }

    @Bean
    public RemoverProduto removerProduto(final ProdutoRepositorio produtoRepositorio) {
        return new RemoverProduto(produtoRepositorio);
    }
}
