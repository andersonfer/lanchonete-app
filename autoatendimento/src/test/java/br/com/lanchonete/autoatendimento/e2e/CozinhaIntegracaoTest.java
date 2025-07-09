package br.com.lanchonete.autoatendimento.e2e;

import br.com.lanchonete.autoatendimento.adaptadores.persistencia.ClienteGatewayJDBC;
import br.com.lanchonete.autoatendimento.adaptadores.persistencia.PedidoGatewayJDBC;
import br.com.lanchonete.autoatendimento.adaptadores.persistencia.ProdutoGatewayJDBC;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
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

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CozinhaIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoGatewayJDBC pedidoGateway;

    @Autowired
    private ClienteGatewayJDBC clienteGateway;

    @Autowired
    private ProdutoGatewayJDBC produtoGateway;

    @Autowired
    private ObjectMapper objectMapper;

    private Long clienteId;
    private Long produtoId;
    private Long pedidoId;

    @BeforeEach
    void configurar() throws Exception {
        // Criar cliente
        String clienteJson = """
            {
                "nome": "João Silva",
                "email": "joao@email.com",
                "cpf": "12345678901"
            }
        """;

        String clienteResponse = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        clienteId = objectMapper.readTree(clienteResponse).get("id").asLong();

        // Criar produto
        String produtoJson = """
            {
                "nome": "X-Bacon",
                "descricao": "Hambúrguer com bacon",
                "preco": 25.90,
                "categoria": "LANCHE"
            }
        """;

        String produtoResponse = mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        produtoId = objectMapper.readTree(produtoResponse).get("id").asLong();

        // Criar pedido
        String pedidoJson = String.format("""
            {
                "cpfCliente": "12345678901",
                "itens": [
                    {
                        "produtoId": %d,
                        "quantidade": 1
                    }
                ]
            }
        """, produtoId);

        String pedidoResponse = mockMvc.perform(post("/pedidos/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        pedidoId = objectMapper.readTree(pedidoResponse).get("id").asLong();
    }

    @Test
    @DisplayName("Deve listar pedidos da cozinha apenas com pagamento aprovado")
    void t1() throws Exception {
        // Verificar que pedido não aparece na cozinha com pagamento pendente
        mockMvc.perform(get("/pedidos/cozinha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Verificar status no banco através do gateway
        Optional<Pedido> pedidoOpt = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoOpt.isPresent());
        assertEquals(StatusPagamento.PENDENTE, pedidoOpt.get().getStatusPagamento());

        // Aprovar pagamento via webhook
        String webhookJson = String.format("""
            {
                "pedidoId": %d,
                "statusPagamento": "APROVADO"
            }
        """, pedidoId);

        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookJson))
                .andExpect(status().isOk());

        // Verificar status de pagamento atualizado no banco
        Optional<Pedido> pedidoAtualizadoOpt = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoAtualizadoOpt.isPresent());
        assertEquals(StatusPagamento.APROVADO, pedidoAtualizadoOpt.get().getStatusPagamento());

        // Verificar que agora aparece na lista da cozinha
        mockMvc.perform(get("/pedidos/cozinha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$[0].status", is("RECEBIDO")))
                .andExpect(jsonPath("$[0].statusPagamento", is("APROVADO")));
    }

    @Test
    @DisplayName("Deve atualizar status do pedido de RECEBIDO para EM_PREPARACAO")
    void t2() throws Exception {
        // Aprovar pagamento primeiro
        aprovarPagamento(pedidoId);

        // Verificar status inicial no banco
        Optional<Pedido> pedidoOpt = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoOpt.isPresent());
        assertEquals(StatusPedido.RECEBIDO, pedidoOpt.get().getStatus());

        // Atualizar status para EM_PREPARACAO
        String atualizarStatusJson = """
            {
                "status": "EM_PREPARACAO"
            }
        """;

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$.status", is("EM_PREPARACAO")))
                .andExpect(jsonPath("$.statusPagamento", is("APROVADO")));

        // Verificar status atualizado no banco
        Optional<Pedido> pedidoAtualizadoOpt = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoAtualizadoOpt.isPresent());
        assertEquals(StatusPedido.EM_PREPARACAO, pedidoAtualizadoOpt.get().getStatus());
    }

    @Test
    @DisplayName("Deve atualizar status do pedido através do fluxo completo")
    void t3() throws Exception {
        // Aprovar pagamento primeiro
        aprovarPagamento(pedidoId);

        // Verificar status inicial
        assertEquals(StatusPedido.RECEBIDO, obterStatusPedido(pedidoId));

        // RECEBIDO → EM_PREPARACAO
        atualizarStatusPedido(pedidoId, StatusPedido.EM_PREPARACAO);
        assertEquals(StatusPedido.EM_PREPARACAO, obterStatusPedido(pedidoId));

        // EM_PREPARACAO → PRONTO
        atualizarStatusPedido(pedidoId, StatusPedido.PRONTO);
        assertEquals(StatusPedido.PRONTO, obterStatusPedido(pedidoId));

        // PRONTO → FINALIZADO
        atualizarStatusPedido(pedidoId, StatusPedido.FINALIZADO);
        assertEquals(StatusPedido.FINALIZADO, obterStatusPedido(pedidoId));

        // Verificar que pedido finalizado não aparece na lista da cozinha
        mockMvc.perform(get("/pedidos/cozinha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Deve rejeitar transição inválida de status")
    void t4() throws Exception {
        // Aprovar pagamento primeiro
        aprovarPagamento(pedidoId);

        // Verificar status inicial
        assertEquals(StatusPedido.RECEBIDO, obterStatusPedido(pedidoId));

        // Tentar transição inválida RECEBIDO → PRONTO
        String atualizarStatusJson = """
            {
                "status": "PRONTO"
            }
        """;

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Transição inválida de RECEBIDO para PRONTO"));

        // Verificar que status não foi alterado no banco
        assertEquals(StatusPedido.RECEBIDO, obterStatusPedido(pedidoId));
    }

    @Test
    @DisplayName("Deve rejeitar atualização de pedido sem pagamento aprovado")
    void t5() throws Exception {
        // Verificar que pedido tem pagamento pendente
        assertEquals(StatusPagamento.PENDENTE, obterStatusPagamento(pedidoId));

        // Tentar atualizar status sem pagamento aprovado
        String atualizarStatusJson = """
            {
                "status": "EM_PREPARACAO"
            }
        """;

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Só é possível atualizar status de pedidos com pagamento aprovado"));

        // Verificar que status não foi alterado no banco
        assertEquals(StatusPedido.RECEBIDO, obterStatusPedido(pedidoId));
    }

    @Test
    @DisplayName("Deve rejeitar atualização de pedido finalizado")
    void t6() throws Exception {
        // Aprovar pagamento e finalizar pedido
        aprovarPagamento(pedidoId);
        atualizarStatusPedido(pedidoId, StatusPedido.EM_PREPARACAO);
        atualizarStatusPedido(pedidoId, StatusPedido.PRONTO);
        atualizarStatusPedido(pedidoId, StatusPedido.FINALIZADO);

        // Verificar que pedido está finalizado
        assertEquals(StatusPedido.FINALIZADO, obterStatusPedido(pedidoId));

        // Tentar atualizar pedido finalizado
        String atualizarStatusJson = """
            {
                "status": "PRONTO"
            }
        """;

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Não é possível alterar status de pedido finalizado"));

        // Verificar que status permaneceu finalizado
        assertEquals(StatusPedido.FINALIZADO, obterStatusPedido(pedidoId));
    }

    @Test
    @DisplayName("Deve manter ordenação correta na lista da cozinha")
    void t7() throws Exception {
        // Criar segundo pedido
        Long segundoPedidoId = criarPedido();

        // Aprovar pagamentos
        aprovarPagamento(pedidoId);
        aprovarPagamento(segundoPedidoId);

        // Atualizar primeiro pedido para PRONTO
        atualizarStatusPedido(pedidoId, StatusPedido.EM_PREPARACAO);
        atualizarStatusPedido(pedidoId, StatusPedido.PRONTO);

        // Manter segundo pedido em EM_PREPARACAO
        atualizarStatusPedido(segundoPedidoId, StatusPedido.EM_PREPARACAO);

        // Verificar ordenação: PRONTO deve vir antes de EM_PREPARACAO
        mockMvc.perform(get("/pedidos/cozinha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$[0].status", is("PRONTO")))
                .andExpect(jsonPath("$[1].id", is(segundoPedidoId.intValue())))
                .andExpect(jsonPath("$[1].status", is("EM_PREPARACAO")));
    }

    @Test
    @DisplayName("Deve retornar erro 404 para pedido inexistente")
    void t8() throws Exception {
        Long pedidoInexistente = 999L;

        String atualizarStatusJson = """
            {
                "status": "EM_PREPARACAO"
            }
        """;

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoInexistente + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{erro=Pedido não encontrado com ID: " + pedidoInexistente + "}"));
    }

    @Test
    @DisplayName("Deve filtrar corretamente pedidos finalizados da lista da cozinha")
    void t9() throws Exception {
        // Criar segundo pedido
        Long segundoPedidoId = criarPedido();

        // Aprovar pagamentos
        aprovarPagamento(pedidoId);
        aprovarPagamento(segundoPedidoId);

        // Finalizar primeiro pedido
        atualizarStatusPedido(pedidoId, StatusPedido.EM_PREPARACAO);
        atualizarStatusPedido(pedidoId, StatusPedido.PRONTO);
        atualizarStatusPedido(pedidoId, StatusPedido.FINALIZADO);

        // Manter segundo pedido em EM_PREPARACAO
        atualizarStatusPedido(segundoPedidoId, StatusPedido.EM_PREPARACAO);

        // Verificar que apenas o pedido não finalizado aparece
        mockMvc.perform(get("/pedidos/cozinha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(segundoPedidoId.intValue())))
                .andExpect(jsonPath("$[0].status", is("EM_PREPARACAO")));

        // Verificar que pedido finalizado realmente está finalizado no banco
        assertEquals(StatusPedido.FINALIZADO, obterStatusPedido(pedidoId));
    }

    @Test
    @DisplayName("Deve permitir manter o mesmo status")
    void t10() throws Exception {
        // Aprovar pagamento primeiro
        aprovarPagamento(pedidoId);

        // Verificar status inicial
        assertEquals(StatusPedido.RECEBIDO, obterStatusPedido(pedidoId));

        // Manter o mesmo status
        String atualizarStatusJson = """
            {
                "status": "RECEBIDO"
            }
        """;

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoId.intValue())))
                .andExpect(jsonPath("$.status", is("RECEBIDO")));

        // Verificar que status permaneceu o mesmo
        assertEquals(StatusPedido.RECEBIDO, obterStatusPedido(pedidoId));
    }

    // Métodos auxiliares

    private void aprovarPagamento(Long pedidoId) throws Exception {
        String webhookJson = String.format("""
            {
                "pedidoId": %d,
                "statusPagamento": "APROVADO"
            }
        """, pedidoId);

        mockMvc.perform(post("/webhook/pagamento")
                .contentType(MediaType.APPLICATION_JSON)
                .content(webhookJson))
                .andExpect(status().isOk());
    }

    private void atualizarStatusPedido(Long pedidoId, StatusPedido novoStatus) throws Exception {
        String atualizarStatusJson = String.format("""
            {
                "status": "%s"
            }
        """, novoStatus.name());

        mockMvc.perform(put("/pedidos/cozinha/" + pedidoId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(atualizarStatusJson))
                .andExpect(status().isOk());
    }

    private StatusPedido obterStatusPedido(Long pedidoId) {
        Optional<Pedido> pedidoOpt = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoOpt.isPresent());
        return pedidoOpt.get().getStatus();
    }

    private StatusPagamento obterStatusPagamento(Long pedidoId) {
        Optional<Pedido> pedidoOpt = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoOpt.isPresent());
        return pedidoOpt.get().getStatusPagamento();
    }

    private Long criarPedido() throws Exception {
        String pedidoJson = String.format("""
            {
                "cpfCliente": "12345678901",
                "itens": [
                    {
                        "produtoId": %d,
                        "quantidade": 2
                    }
                ]
            }
        """, produtoId);

        String pedidoResponse = mockMvc.perform(post("/pedidos/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(pedidoResponse).get("id").asLong();
    }
}