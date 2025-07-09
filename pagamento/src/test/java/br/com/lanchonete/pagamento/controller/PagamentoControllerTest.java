package br.com.lanchonete.pagamento.controller;

import br.com.lanchonete.pagamento.dto.PagamentoRequestDTO;
import br.com.lanchonete.pagamento.dto.PagamentoResponseDTO;
import br.com.lanchonete.pagamento.service.PagamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagamentoController.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagamentoService pagamentoService;

    private PagamentoRequestDTO requestValido;
    private PagamentoResponseDTO responseValido;

    @BeforeEach
    void configurar() {
        requestValido = new PagamentoRequestDTO("123", new BigDecimal("25.50"));
        responseValido = new PagamentoResponseDTO("123", "PENDENTE");
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso quando dados são válidos")
    void t1() throws Exception {
        when(pagamentoService.processarPagamento(any(PagamentoRequestDTO.class))).thenReturn(responseValido);

        mockMvc.perform(post("/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value("123"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("Deve retornar status 200 quando processar pagamento")
    void t2() throws Exception {
        when(pagamentoService.processarPagamento(any(PagamentoRequestDTO.class))).thenReturn(responseValido);

        mockMvc.perform(post("/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve aceitar diferentes valores de pagamento quando dados são válidos")
    void t3() throws Exception {
        PagamentoRequestDTO requestDiferente = new PagamentoRequestDTO("456", new BigDecimal("100.00"));
        PagamentoResponseDTO responseDiferente = new PagamentoResponseDTO("456", "PENDENTE");
        
        when(pagamentoService.processarPagamento(any(PagamentoRequestDTO.class))).thenReturn(responseDiferente);

        mockMvc.perform(post("/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDiferente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value("456"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
}