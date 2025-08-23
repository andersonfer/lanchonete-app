package br.com.lanchonete.produtos.aplicacao.casosdeuso;

import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.entidades.Produto;

import java.util.List;

public class ListarProdutos {
    
    private final ProdutoGateway produtoGateway;
    
    public ListarProdutos(ProdutoGateway produtoGateway) {
        this.produtoGateway = produtoGateway;
    }
    
    public List<Produto> executar() {
        return produtoGateway.listarTodos();
    }
}