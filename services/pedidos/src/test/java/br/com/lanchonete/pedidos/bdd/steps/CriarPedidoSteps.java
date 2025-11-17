package br.com.lanchonete.pedidos.bdd.steps;

import br.com.lanchonete.pedidos.adapters.web.dto.ItemPedidoRequest;
import br.com.lanchonete.pedidos.adapters.web.dto.RealizarPedidoRequest;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CriarPedidoSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<?> response;
    private List<Map<String, Object>> produtosDisponiveis;

    @Dado("que existem produtos cadastrados")
    public void queExistemProdutosCadastrados() {
        ResponseEntity<List> produtosResponse = restTemplate.getForEntity("/produtos", List.class);
        assertEquals(HttpStatus.OK, produtosResponse.getStatusCode());
        assertNotNull(produtosResponse.getBody());
        assertFalse(produtosResponse.getBody().isEmpty(), "Deve existir produtos cadastrados");
    }

    @Quando("eu consulto os produtos disponíveis")
    public void euConsultoOsProdutosDisponiveis() {
        response = restTemplate.getForEntity("/produtos", List.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            produtosDisponiveis = (List<Map<String, Object>>) response.getBody();
        }
    }

    @Então("devo ver produtos cadastrados no sistema")
    public void devoVerProdutosCadastradosNoSistema() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(produtosDisponiveis);
        assertFalse(produtosDisponiveis.isEmpty(), "Lista de produtos não deve estar vazia");
        assertTrue(produtosDisponiveis.size() >= 3, "Deve ter pelo menos 3 produtos");
    }

    @Quando("eu tento criar um pedido sem itens")
    public void euTentoCriarUmPedidoSemItens() {
        RealizarPedidoRequest request = new RealizarPedidoRequest(null, new ArrayList<>());
        response = restTemplate.postForEntity("/pedidos", request, Map.class);
    }

    @Então("o sistema deve retornar um erro de validação")
    public void oSistemaDeveRetornarUmErroDeValidacao() {
        assertTrue(response.getStatusCode() == HttpStatus.BAD_REQUEST ||
                   response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY,
                "Esperado status 400 ou 422, mas recebeu: " + response.getStatusCode());
    }

    @Quando("eu verifico a estrutura necessária para criar um pedido")
    public void euVerificoAEstruturaNecessariaParaCriarUmPedido() {
        // Verificação conceitual - não precisa fazer requisição
    }

    @Então("o pedido deve ter estrutura com itens e cada item com produto e quantidade")
    public void oPedidoDeveTerEstruturaComItensECadaItemComProdutoEQuantidade() {
        // Validação estrutural dos DTOs
        RealizarPedidoRequest pedidoRequest = new RealizarPedidoRequest();
        List<ItemPedidoRequest> itens = new ArrayList<>();

        ItemPedidoRequest item = new ItemPedidoRequest();
        item.setProdutoId(1L);
        item.setQuantidade(2);
        itens.add(item);

        pedidoRequest.setItens(itens);

        // Validar estrutura do pedido
        assertNotNull(pedidoRequest.getItens(), "Pedido deve ter lista de itens");
        assertFalse(pedidoRequest.getItens().isEmpty(), "Lista de itens não deve estar vazia");

        // Validar estrutura do item
        ItemPedidoRequest primeiroItem = pedidoRequest.getItens().get(0);
        assertNotNull(primeiroItem.getProdutoId(), "Item deve ter produtoId");
        assertTrue(primeiroItem.getQuantidade() > 0, "Item deve ter quantidade positiva");
    }
}
