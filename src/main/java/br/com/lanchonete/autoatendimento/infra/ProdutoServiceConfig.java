package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.BuscarProdutosPorCategoriaService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.CriarProdutoService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.EditarProdutoService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.RemoverProdutoService;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoServiceConfig {

    @Bean
    public BuscarProdutosPorCategoriaService buscarProdutosPorCategoriaService(ProdutoRepositorio produtoRepositorio) {
        return new BuscarProdutosPorCategoriaService(produtoRepositorio);
    }

    @Bean
    public CriarProdutoService criarProdutoService(ProdutoRepositorio produtoRepositorio) {
        return new CriarProdutoService(produtoRepositorio);
    }

    @Bean
    public EditarProdutoService editarProdutoService(ProdutoRepositorio produtoRepositorio) {
        return new EditarProdutoService(produtoRepositorio);
    }

    @Bean
    public RemoverProdutoService removerProdutoService(ProdutoRepositorio produtoRepositorio) {
        return new RemoverProdutoService(produtoRepositorio);
    }
}
