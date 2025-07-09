package br.com.lanchonete.autoatendimento.e2e;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.WebhookPagamentoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebhookIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoGateway pedidoGateway;

    @Autowired
    private ClienteGateway clienteGateway;

    @Autowired
    private ProdutoGateway produtoGateway;

    private Cliente cliente;
    private Produto produto;
    private Pedido pedido;

    @BeforeEach
    void configurar() {
        // Criar cliente
        cliente = Cliente.criar("João Silva", "joao@teste.com", "12345678901");
        cliente = clienteGateway.salvar(cliente);

        // Criar produto
        produto = Produto.criar("X-Bacon", "Hambúrguer com bacon", new BigDecimal("25.90"), Categoria.LANCHE);
        produto = produtoGateway.salvar(produto);

        // Criar pedido
        pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produto, 2);
        pedido.adicionarItem(item);
        pedido = pedidoGateway.salvar(pedido);
    }

    @Test
    @DisplayName("Deve aprovar pagamento via webhook e verificar alteração no banco")
    void t1() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Verificar status inicial
        Pedido pedidoInicial = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.PENDENTE, pedidoInicial.getStatusPagamento());

        // Act - Enviar webhook
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        // Assert - Verificar alteração no banco
        Pedido pedidoAtualizado = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.APROVADO, pedidoAtualizado.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve rejeitar pagamento via webhook e verificar alteração no banco")
    void t2() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "REJEITADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Verificar status inicial
        Pedido pedidoInicial = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.PENDENTE, pedidoInicial.getStatusPagamento());

        // Act - Enviar webhook
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        // Assert - Verificar alteração no banco
        Pedido pedidoAtualizado = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.REJEITADO, pedidoAtualizado.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve retornar 400 quando tentar aprovar pagamento já processado")
    void t3() throws Exception {
        // Arrange - Aprovar pagamento primeiro
        pedido.aprovarPagamento();
        pedidoGateway.atualizarStatusPagamento(pedido.getId(), StatusPagamento.APROVADO);

        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Verificar que status não mudou
        Pedido pedidoFinal = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.APROVADO, pedidoFinal.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve retornar 404 quando pedido não existir")
    void t4() throws Exception {
        // Arrange
        Long pedidoIdInexistente = 999L;
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedidoIdInexistente, "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());

        // Verificar que pedido original não foi afetado
        Pedido pedidoOriginal = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.PENDENTE, pedidoOriginal.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve retornar 400 quando status de pagamento for inválido")
    void t5() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "INVALIDO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Verificar que status não mudou
        Pedido pedidoFinal = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.PENDENTE, pedidoFinal.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve processar múltiplos webhooks sequenciais corretamente")
    void t6() throws Exception {
        // Criar segundo pedido
        Pedido segundoPedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produto, 1);
        segundoPedido.adicionarItem(item);
        segundoPedido = pedidoGateway.salvar(segundoPedido);

        // Arrange - Webhooks para dois pedidos
        WebhookPagamentoDTO webhook1 = new WebhookPagamentoDTO(pedido.getId(), "APROVADO");
        WebhookPagamentoDTO webhook2 = new WebhookPagamentoDTO(segundoPedido.getId(), "REJEITADO");

        String request1Json = objectMapper.writeValueAsString(webhook1);
        String request2Json = objectMapper.writeValueAsString(webhook2);

        // Act - Processar primeiro webhook
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request1Json))
                .andExpect(status().isOk());

        // Act - Processar segundo webhook
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request2Json))
                .andExpect(status().isOk());

        // Assert - Verificar ambos os pedidos no banco
        Pedido pedido1Final = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        Pedido pedido2Final = pedidoGateway.buscarPorId(segundoPedido.getId()).orElseThrow();

        assertEquals(StatusPagamento.APROVADO, pedido1Final.getStatusPagamento());
        assertEquals(StatusPagamento.REJEITADO, pedido2Final.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve ser idempotente - mesmo webhook múltiplas vezes deve falhar na segunda tentativa")
    void t7() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act - Primeira chamada deve funcionar
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        // Assert - Verificar que foi aprovado
        Pedido pedidoAprovado = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.APROVADO, pedidoAprovado.getStatusPagamento());

        // Act - Segunda chamada deve falhar
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Assert - Status deve continuar aprovado
        Pedido pedidoFinal = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.APROVADO, pedidoFinal.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve funcionar com pedido sem cliente (cliente anônimo)")
    void t8() throws Exception {
        // Arrange - Criar pedido sem cliente
        Pedido pedidoAnonimo = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedidoAnonimo.adicionarItem(item);
        pedidoAnonimo = pedidoGateway.salvar(pedidoAnonimo);

        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedidoAnonimo.getId(), "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Verificar status inicial
        Pedido pedidoInicial = pedidoGateway.buscarPorId(pedidoAnonimo.getId()).orElseThrow();
        assertEquals(StatusPagamento.PENDENTE, pedidoInicial.getStatusPagamento());

        // Act - Enviar webhook
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        // Assert - Verificar alteração no banco
        Pedido pedidoAtualizado = pedidoGateway.buscarPorId(pedidoAnonimo.getId()).orElseThrow();
        assertEquals(StatusPagamento.APROVADO, pedidoAtualizado.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve aceitar status em lowercase e converter corretamente")
    void t9() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "aprovado");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act - Enviar webhook
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        // Assert - Verificar alteração no banco
        Pedido pedidoAtualizado = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.APROVADO, pedidoAtualizado.getStatusPagamento());
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de alterar para status PENDENTE")
    void t10() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(pedido.getId(), "PENDENTE");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Verificar que status não mudou
        Pedido pedidoFinal = pedidoGateway.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(StatusPagamento.PENDENTE, pedidoFinal.getStatusPagamento());
    }
}