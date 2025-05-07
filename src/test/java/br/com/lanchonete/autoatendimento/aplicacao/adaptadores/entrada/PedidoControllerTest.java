package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.adaptadores.rest.PedidoController;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ItemPedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.pedido.ListarPedidosUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.pedido.RealizarPedidoUC;
import br.com.lanchonete.autoatendimento.dominio.modelo.StatusPedido;
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
    private RealizarPedidoUC realizarPedidoUC;

    @MockitoBean
    private ListarPedidosUC listarPedidosUC;

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
                1L,
                "João Silva",
                itensResponse,
                StatusPedido.RECEBIDO,
                LocalDateTime.now(),
                new BigDecimal("57.80")
        );

        // Criar objeto de resposta sem cliente
        pedidoResponseSemCliente = new PedidoResponseDTO(
                2L,
                null,
                null,
                Collections.singletonList(itensResponse.get(1)),
                StatusPedido.RECEBIDO,
                LocalDateTime.now(),
                new BigDecimal("6.00")
        );
    }

    @Test
    @DisplayName("Deve realizar checkout com sucesso")
    void t1() throws Exception {
        // Mock do serviço
        when(realizarPedidoUC.executar(any(PedidoRequestDTO.class)))
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
                .andExpect(jsonPath("$.valorTotal").value(57.80))
                .andExpect(jsonPath("$.itens.length()").value(2));

        // Verificar que o serviço foi chamado
        verify(realizarPedidoUC).executar(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao realizar checkout com dados inválidos")
    void t2() throws Exception {
        // Mock do serviço lançando exceção de validação
        when(realizarPedidoUC.executar(any(PedidoRequestDTO.class)))
                .thenThrow(new ValidacaoException("Pedido deve conter pelo menos um item"));

        // Executar e verificar
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pedido deve conter pelo menos um item"));

        // Verificar que o serviço foi chamado
        verify(realizarPedidoUC).executar(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao realizar checkout com cliente ou produto não encontrado")
    void t3() throws Exception {
        // Mock do serviço lançando exceção de recurso não encontrado
        when(realizarPedidoUC.executar(any(PedidoRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado com o CPF informado"));

        // Executar e verificar
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isNotFound());

        // Verificar que o serviço foi chamado
        verify(realizarPedidoUC).executar(any(PedidoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void t4() throws Exception {
        // Mock do serviço
        when(listarPedidosUC.executar())
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
        verify(listarPedidosUC).executar();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    void t5() throws Exception {
        // Mock do serviço
        when(listarPedidosUC.executar())
                .thenReturn(Collections.emptyList());

        // Executar e verificar
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verificar que o serviço foi chamado
        verify(listarPedidosUC).executar();
    }
}