package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.WebhookPagamentoDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.servicos.WebhookService;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebhookService webhookService;

    @Test
    @DisplayName("Deve processar webhook de pagamento aprovado com sucesso")
    void t1() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(1L, "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

    @Test
    @DisplayName("Deve processar webhook de pagamento rejeitado com sucesso")
    void t2() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(1L, "REJEITADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

    @Test
    @DisplayName("Deve retornar 400 quando pedido não for encontrado")
    void t3() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(999L, "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        doThrow(new RecursoNaoEncontradoException("Pedido não encontrado: 999"))
                .when(webhookService).processarWebhookPagamento(any());

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

    @Test
    @DisplayName("Deve retornar 400 quando pagamento já foi processado")
    void t4() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(1L, "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        doThrow(new ValidacaoException("Pagamento do pedido 1 já foi processado"))
                .when(webhookService).processarWebhookPagamento(any());

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

    @Test
    @DisplayName("Deve retornar 400 quando status de pagamento for inválido")
    void t5() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(1L, "INVALIDO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        doThrow(new ValidacaoException("Status de pagamento inválido: INVALIDO"))
                .when(webhookService).processarWebhookPagamento(any());

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

    @Test
    @DisplayName("Deve retornar 400 quando request for inválido - pedidoId null")
    void t6() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(null, "APROVADO");
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        doThrow(new ValidacaoException("PedidoId não pode ser null"))
                .when(webhookService).processarWebhookPagamento(any());

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

    @Test
    @DisplayName("Deve retornar 400 quando request for inválido - statusPagamento null")
    void t7() throws Exception {
        // Arrange
        WebhookPagamentoDTO webhookRequest = new WebhookPagamentoDTO(1L, null);
        String requestJson = objectMapper.writeValueAsString(webhookRequest);

        doThrow(new ValidacaoException("StatusPagamento não pode ser null"))
                .when(webhookService).processarWebhookPagamento(any());

        // Act & Assert
        mockMvc.perform(post("/webhook/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(webhookService).processarWebhookPagamento(webhookRequest);
    }

}