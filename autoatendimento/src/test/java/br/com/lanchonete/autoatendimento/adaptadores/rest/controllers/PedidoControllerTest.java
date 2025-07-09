package br.com.lanchonete.autoatendimento.adaptadores.rest.controllers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ItemPedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPagamentoResponseDTO;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.dominio.excecoes.ValidacaoException;
import br.com.lanchonete.autoatendimento.adaptadores.rest.servicos.PedidoService;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    private PedidoRequestDTO pedidoRequest;
    private PedidoResponseDTO pedidoResponse;
    private PedidoResponseDTO pedidoResponseSemCliente;

    @BeforeEach
    void configurar() {
        // Criar objeto de requisição para o teste
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(1L, 2),
                new ItemPedidoDTO(2L, 1)
        );
        pedidoRequest = new PedidoRequestDTO("12345678901", itens);

        // Criar objeto de resposta para o teste com cliente
        List<ItemPedidoResponseDTO> itensResponse = Arrays.asList(
                new ItemPedidoResponseDTO(1L, 1L, "X-Bacon", "Hambúrguer com bacon", 2,
                        new BigDecimal("25.90"), new BigDecimal("51.80")),
                new ItemPedidoResponseDTO(2L, 2L, "Refrigerante", "Refrigerante lata 350ml", 1,
                        new BigDecimal("6.00"), new BigDecimal("6.00"))
        );

        pedidoResponse = new PedidoResponseDTO(
                1L,
                "PED000001",
                1L,
                "João Silva",
                itensResponse,
                StatusPedido.RECEBIDO,
                StatusPagamento.PENDENTE,
                LocalDateTime.now(),
                new BigDecimal("57.80")
        );

        // Criar objeto de resposta sem cliente
        pedidoResponseSemCliente = new PedidoResponseDTO(
                2L,
                "PED000002",
                null,
                null,
                Collections.singletonList(itensResponse.get(1)),
                StatusPedido.RECEBIDO,
                StatusPagamento.PENDENTE,
                LocalDateTime.now(),
                new BigDecimal("6.00")
        );
    }

    @Test
    @DisplayName("Deve realizar checkout com sucesso")
    void t1() throws Exception {
        // Mock do serviço
        when(pedidoService.realizarCheckout(any(PedidoRequestDTO.class)))
                .thenReturn(pedidoResponse);

        // Executar e verificar
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"))
                .andExpect(jsonPath("$.valorTotal").value(57.80))
                .andExpect(jsonPath("$.itens.length()").value(2));

        // Verificar que o serviço foi chamado
        verify(pedidoService).realizarCheckout(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao realizar checkout com dados inválidos")
    void t2() throws Exception {
        // Mock do serviço lançando exceção de validação
        when(pedidoService.realizarCheckout(any(PedidoRequestDTO.class)))
                .thenThrow(new ValidacaoException("Pedido deve conter pelo menos um item"));

        // Executar e verificar
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pedido deve conter pelo menos um item"));

        // Verificar que o serviço foi chamado
        verify(pedidoService).realizarCheckout(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao realizar checkout com cliente ou produto não encontrado")
    void t3() throws Exception {
        // Mock do serviço lançando exceção de recurso não encontrado
        when(pedidoService.realizarCheckout(any(PedidoRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado com o CPF informado"));

        // Executar e verificar
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isNotFound());

        // Verificar que o serviço foi chamado
        verify(pedidoService).realizarCheckout(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void t4() throws Exception {
        // Mock do serviço
        when(pedidoService.listarPedidos())
                .thenReturn(Arrays.asList(pedidoResponse, pedidoResponseSemCliente));

        // Executar e verificar
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(1))
                .andExpect(jsonPath("$[0].nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$[0].valorTotal").value(57.80))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].clienteId").isEmpty())
                .andExpect(jsonPath("$[1].nomeCliente").isEmpty())
                .andExpect(jsonPath("$[1].valorTotal").value(6.00));

        // Verificar que o serviço foi chamado
        verify(pedidoService).listarPedidos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void t5() throws Exception {
        // Mock do serviço
        when(pedidoService.listarPedidos())
                .thenReturn(Collections.emptyList());

        // Executar e verificar
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verificar que o serviço foi chamado
        verify(pedidoService).listarPedidos();
    }

    @Test
    @DisplayName("Deve consultar status de pagamento PENDENTE com sucesso")
    void t6() throws Exception {
        // Mock do serviço
        StatusPagamentoResponseDTO statusResponse = StatusPagamentoResponseDTO.pendente(1L);
        when(pedidoService.consultarStatusPagamento(1L))
                .thenReturn(statusResponse);

        // Executar e verificar
        mockMvc.perform(get("/pedidos/1/pagamento/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(1))
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("Pagamento pendente de processamento"));

        // Verificar que o serviço foi chamado
        verify(pedidoService).consultarStatusPagamento(1L);
    }

    @Test
    @DisplayName("Deve consultar status de pagamento APROVADO com sucesso")
    void t7() throws Exception {
        // Mock do serviço
        StatusPagamentoResponseDTO statusResponse = StatusPagamentoResponseDTO.aprovado(2L);
        when(pedidoService.consultarStatusPagamento(2L))
                .thenReturn(statusResponse);

        // Executar e verificar
        mockMvc.perform(get("/pedidos/2/pagamento/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(2))
                .andExpect(jsonPath("$.statusPagamento").value("APROVADO"))
                .andExpect(jsonPath("$.mensagem").value("Pagamento aprovado com sucesso"));

        // Verificar que o serviço foi chamado
        verify(pedidoService).consultarStatusPagamento(2L);
    }

    @Test
    @DisplayName("Deve consultar status de pagamento REJEITADO com sucesso")
    void t8() throws Exception {
        // Mock do serviço
        StatusPagamentoResponseDTO statusResponse = StatusPagamentoResponseDTO.rejeitado(3L);
        when(pedidoService.consultarStatusPagamento(3L))
                .thenReturn(statusResponse);

        // Executar e verificar
        mockMvc.perform(get("/pedidos/3/pagamento/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(3))
                .andExpect(jsonPath("$.statusPagamento").value("REJEITADO"))
                .andExpect(jsonPath("$.mensagem").value("Pagamento rejeitado"));

        // Verificar que o serviço foi chamado
        verify(pedidoService).consultarStatusPagamento(3L);
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao consultar status de pagamento de pedido inexistente")
    void t9() throws Exception {
        // Mock do serviço lançando exceção de recurso não encontrado
        when(pedidoService.consultarStatusPagamento(999L))
                .thenThrow(new RecursoNaoEncontradoException("Pedido não encontrado com ID: 999"));

        // Executar e verificar
        mockMvc.perform(get("/pedidos/999/pagamento/status"))
                .andExpect(status().isNotFound());

        // Verificar que o serviço foi chamado
        verify(pedidoService).consultarStatusPagamento(999L);
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao consultar status com ID inválido")
    void t10() throws Exception {
        // Executar e verificar (Spring converterá automaticamente)
        mockMvc.perform(get("/pedidos/abc/pagamento/status"))
                .andExpect(status().isBadRequest());
    }
}