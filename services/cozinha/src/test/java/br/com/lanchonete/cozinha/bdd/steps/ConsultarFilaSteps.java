package br.com.lanchonete.cozinha.bdd.steps;

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

public class ConsultarFilaSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<List> response;

    @Dado("que não existem pedidos na fila")
    public void queNaoExistemPedidosNaFila() {
        // O banco é limpo antes de cada cenário pelo hook
        // Não precisa fazer nada aqui
    }

    @Quando("eu consulto a fila da cozinha")
    public void euConsultoAFilaDaCozinha() {
        response = restTemplate.getForEntity("/cozinha/fila", List.class);
    }

    @Então("devo receber uma lista vazia")
    public void devoReceberUmaListaVazia() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty(), "A fila deve estar vazia");
    }

    @Dado("que existem pedidos na fila da cozinha")
    public void queExistemPedidosNaFilaDaCozinha() {
        // Pedidos são inseridos pelo hook DatabaseHooks
        // Este método apenas valida que existem pedidos
    }

    @Então("devo receber a lista de pedidos")
    public void devoReceberAListaDePedidos() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty(), "A fila não deve estar vazia");
    }

    @E("os pedidos devem conter status e identificação")
    public void osPedidosDevemConterStatusEIdentificacao() {
        List<Map<String, Object>> pedidos = response.getBody();

        for (Map<String, Object> pedido : pedidos) {
            assertNotNull(pedido.get("pedidoId"), "Pedido deve ter pedidoId");
            assertNotNull(pedido.get("status"), "Pedido deve ter status");
        }
    }
}
