package br.com.lanchonete.clientes.bdd.steps;

import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.model.Cliente;
import io.cucumber.datatable.DataTable;
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

public class IdentificacaoClienteSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteGateway clienteGateway;

    private ResponseEntity<?> response;

    @Dado("que existe um cliente cadastrado com nome {string}, email {string} e CPF {string}")
    public void queExisteUmClienteCadastradoComNomeEmailECPF(String nome, String email, String cpf) {
        clienteGateway.buscarPorCpf(cpf).orElseGet(() -> {
            Cliente cliente = Cliente.criar(nome, email, cpf);
            return clienteGateway.salvar(cliente);
        });
    }

    @Quando("eu busco um cliente pelo CPF {string}")
    public void euBuscoUmClientePeloCPF(String cpf) {
        response = restTemplate.getForEntity("/clientes/cpf/" + cpf, Map.class);
    }

    @Então("o sistema deve retornar os dados do cliente")
    public void oSistemaDeveRetornarOsDadosDoCliente() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @E("o nome do cliente deve ser {string}")
    public void oNomeDoClienteDeveSer(String nomeEsperado) {
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(nomeEsperado, body.get("nome"));
    }

    @Então("o sistema deve retornar que o cliente não foi encontrado")
    public void oSistemaDeveRetornarQueOClienteNaoFoiEncontrado() {
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Dado("que existem os seguintes clientes cadastrados:")
    public void queExistemOsSeguintesClientesCadastrados(DataTable dataTable) {
        List<Map<String, String>> clientes = dataTable.asMaps();

        for (Map<String, String> clienteData : clientes) {
            String nome = clienteData.get("nome");
            String email = clienteData.get("email");
            String cpf = clienteData.get("cpf");

            clienteGateway.buscarPorCpf(cpf).orElseGet(() -> {
                Cliente cliente = Cliente.criar(nome, email, cpf);
                return clienteGateway.salvar(cliente);
            });
        }
    }

    @Quando("eu listo todos os clientes")
    public void euListoTodosOsClientes() {
        response = restTemplate.getForEntity("/clientes", List.class);
    }

    @Então("o sistema deve retornar {int} clientes")
    public void oSistemaDeveRetornarClientes(int quantidadeEsperada) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> clientes = (List<?>) response.getBody();
        assertNotNull(clientes);
        assertTrue(clientes.size() >= quantidadeEsperada,
                "Esperava pelo menos " + quantidadeEsperada + " clientes, mas encontrou " + clientes.size());
    }
}
