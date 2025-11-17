package br.com.lanchonete.clientes.bdd.steps;

import br.com.lanchonete.clientes.adapters.web.dto.ClienteRequest;
import br.com.lanchonete.clientes.application.gateways.ClienteGateway;
import br.com.lanchonete.clientes.domain.model.Cliente;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CadastroClienteSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteGateway clienteGateway;

    private ResponseEntity<?> response;
    private Long clienteIdCriado;

    @Dado("que não existe um cliente com o CPF {string}")
    public void queNaoExisteUmClienteComOCPF(String cpf) {
        // O hook DatabaseHooks já limpa o banco antes de cada cenário
    }

    @Dado("que existe um cliente com nome {string}, email {string} e CPF {string}")
    public void queExisteUmClienteComNomeEmailECPF(String nome, String email, String cpf) {
        clienteGateway.buscarPorCpf(cpf).orElseGet(() -> {
            Cliente cliente = Cliente.criar(nome, email, cpf);
            return clienteGateway.salvar(cliente);
        });
    }

    @Quando("eu cadastro um cliente com nome {string}, email {string} e CPF {string}")
    public void euCadastroUmClienteComNomeEmailECPF(String nome, String email, String cpf) {
        ClienteRequest request = new ClienteRequest(nome, email, cpf);
        response = restTemplate.postForEntity("/clientes", request, Map.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            clienteIdCriado = ((Number) body.get("id")).longValue();
        }
    }

    @Quando("eu tento cadastrar um cliente com nome {string}, email {string} e CPF {string}")
    public void euTentoCadastrarUmClienteComNomeEmailECPF(String nome, String email, String cpf) {
        ClienteRequest request = new ClienteRequest(nome, email, cpf);
        response = restTemplate.postForEntity("/clientes", request, Map.class);
    }

    @Então("o cliente deve ser cadastrado com sucesso")
    public void oClienteDeveSerCadastradoComSucesso() {
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(clienteIdCriado);
    }

    @E("o cliente deve ter o nome {string}")
    public void oClienteDeveTerONomeCadastrado(String nomeEsperado) {
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(nomeEsperado, body.get("nome"));
    }

    @E("o cliente deve ter o CPF {string}")
    public void oClienteDeveTerOCPF(String cpfEsperado) {
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(cpfEsperado, body.get("cpf"));
    }

    @Então("o sistema deve retornar um erro de CPF duplicado")
    public void oSistemaDeveRetornarUmErroDeCPFDuplicado() {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Body da resposta não deve ser nulo");

        String mensagem = body.get("mensagem") != null ? (String) body.get("mensagem") : "";
        String message = body.get("message") != null ? (String) body.get("message") : "";
        String error = body.get("error") != null ? (String) body.get("error") : "";

        String textoCompleto = mensagem + " " + message + " " + error;
        assertTrue(textoCompleto.toLowerCase().contains("cpf") &&
                  (textoCompleto.toLowerCase().contains("cadastrado") ||
                   textoCompleto.toLowerCase().contains("duplicado") ||
                   textoCompleto.toLowerCase().contains("já existe")),
                "Resposta esperada sobre CPF duplicado, mas obteve: " + body);
    }

    @Então("o sistema deve retornar um erro de CPF inválido")
    public void oSistemaDeveRetornarUmErroDeCPFInvalido() {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Body da resposta não deve ser nulo");

        String mensagem = body.get("mensagem") != null ? (String) body.get("mensagem") : "";
        String message = body.get("message") != null ? (String) body.get("message") : "";
        String error = body.get("error") != null ? (String) body.get("error") : "";

        String textoCompleto = mensagem + " " + message + " " + error;
        assertTrue(textoCompleto.toLowerCase().contains("cpf") || textoCompleto.toLowerCase().contains("dígit"),
                "Resposta esperada sobre CPF inválido, mas obteve: " + body);
    }

    @Então("o sistema deve retornar um erro de validação")
    public void oSistemaDeveRetornarUmErroDeValidacao() {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
