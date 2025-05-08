package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
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
