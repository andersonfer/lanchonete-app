package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.aplicacao.repositorios.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoServiceConfig {

    @Bean
    public BuscarProdutosPorCategoria buscarProdutosPorCategoria(ProdutoRepositorio produtoRepositorio) {
        return new BuscarProdutosPorCategoria(produtoRepositorio);
    }

    @Bean
    public CriarProduto criarProduto(ProdutoRepositorio produtoRepositorio) {
        return new CriarProduto(produtoRepositorio);
    }

    @Bean
    public EditarProduto editarProduto(ProdutoRepositorio produtoRepositorio) {
        return new EditarProduto(produtoRepositorio);
    }

    @Bean
    public RemoverProduto removerProduto(ProdutoRepositorio produtoRepositorio) {
        return new RemoverProduto(produtoRepositorio);
    }
}
