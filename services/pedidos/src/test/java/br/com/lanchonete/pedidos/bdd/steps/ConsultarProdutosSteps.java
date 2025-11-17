package br.com.lanchonete.pedidos.bdd.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConsultarProdutosSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<List> responseLista;
    private ResponseEntity<Map> responseItem;
    private String categoriaFiltro;

    @Dado("que existem produtos cadastrados no sistema")
    public void queExistemProdutosCadastradosNoSistema() {
        // Produtos são cadastrados automaticamente via data.sql
        ResponseEntity<List> response = restTemplate.getForEntity("/produtos", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty(), "Deve existir produtos cadastrados");
    }

    @Dado("que existem produtos de categoria {string}")
    public void queExistemProdutosDeCategoria(String categoria) {
        this.categoriaFiltro = categoria;
        // Produtos são cadastrados automaticamente via data.sql com diferentes categorias
    }

    @Dado("que existe um produto com id {int}")
    public void queExisteUmProdutoComId(int produtoId) {
        ResponseEntity<Map> response = restTemplate.getForEntity("/produtos/" + produtoId, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Quando("eu consulto todos os produtos")
    public void euConsultoTodosOsProdutos() {
        responseLista = restTemplate.getForEntity("/produtos", List.class);
    }

    @Quando("eu consulto produtos da categoria {string}")
    public void euConsultoProdutosDaCategoria(String categoria) {
        responseLista = restTemplate.getForEntity("/produtos?categoria=" + categoria, List.class);
    }

    @Quando("eu consulto o produto com id {int}")
    public void euConsultoOProdutoComId(int produtoId) {
        responseItem = restTemplate.getForEntity("/produtos/" + produtoId, Map.class);
    }

    @Então("devo receber uma lista com produtos")
    public void devoReceberUmaListaComProdutos() {
        assertEquals(HttpStatus.OK, responseLista.getStatusCode());
        assertNotNull(responseLista.getBody());
        assertFalse(responseLista.getBody().isEmpty(), "A lista de produtos não deve estar vazia");
    }

    @E("os produtos devem conter nome e preço")
    public void osProdutosDevemConterNomeEPreco() {
        List<Map<String, Object>> produtos = (List<Map<String, Object>>) responseLista.getBody();

        for (Map<String, Object> produto : produtos) {
            assertNotNull(produto.get("nome"), "Produto deve ter nome");
            assertNotNull(produto.get("preco"), "Produto deve ter preço");
        }
    }

    @Então("devo receber apenas produtos da categoria {string}")
    public void devoReceberApenasProdutosDaCategoria(String categoriaEsperada) {
        assertEquals(HttpStatus.OK, responseLista.getStatusCode());
        List<Map<String, Object>> produtos = (List<Map<String, Object>>) responseLista.getBody();

        assertNotNull(produtos);
        assertFalse(produtos.isEmpty(), "Deve retornar produtos da categoria " + categoriaEsperada);

        for (Map<String, Object> produto : produtos) {
            assertEquals(categoriaEsperada, produto.get("categoria"),
                "Todos os produtos devem ser da categoria " + categoriaEsperada);
        }
    }

    @Então("devo receber os detalhes do produto")
    public void devoReceberOsDetalhesDoProduto() {
        assertEquals(HttpStatus.OK, responseItem.getStatusCode());
        Map<String, Object> produto = responseItem.getBody();

        assertNotNull(produto);
        assertNotNull(produto.get("id"), "Produto deve ter ID");
        assertNotNull(produto.get("nome"), "Produto deve ter nome");
        assertNotNull(produto.get("preco"), "Produto deve ter preço");
        assertNotNull(produto.get("categoria"), "Produto deve ter categoria");
    }
}
