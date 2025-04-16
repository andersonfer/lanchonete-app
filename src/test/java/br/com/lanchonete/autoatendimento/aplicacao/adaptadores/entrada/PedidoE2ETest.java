package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Autowired
    private ProdutoRepositorio produtoRepositorio;

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    private Cliente cliente;
    private Produto produto1;
    private Produto produto2;

    @BeforeEach
    void configurar() {
        // Criar e salvar cliente para o teste
        cliente = Cliente.builder()
                .nome("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .build();
        cliente = clienteRepositorio.salvar(cliente);

        // Criar e salvar produtos para o teste
        produto1 = Produto.builder()
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon")
                .preco(new BigDecimal("25.90"))
                .categoria(Categoria.LANCHE)
                .build();
        produto1 = produtoRepositorio.salvar(produto1);

        produto2 = Produto.builder()
                .nome("Refrigerante")
                .descricao("Refrigerante lata 350ml")
                .preco(new BigDecimal("6.00"))
                .categoria(Categoria.BEBIDA)
                .build();
        produto2 = produtoRepositorio.salvar(produto2);
    }

    @Test
    @DisplayName("Deve realizar checkout de um pedido com cliente e listar o pedido")
    void t1() throws Exception {
        // Criar pedido request
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(produto1.getId(), 2),
                new ItemPedidoDTO(produto2.getId(), 1)
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(cliente.getCpf(), itens);

        // Realizar checkout do pedido
        MvcResult resultado = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.nomeCliente").value(cliente.getNome()))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.valorTotal").value(57.80))
                .andReturn();

        // Extrair ID do pedido da resposta
        String respostaJson = resultado.getResponse().getContentAsString();
        PedidoResponseDTO pedidoResponse = objectMapper.readValue(respostaJson, PedidoResponseDTO.class);
        Long pedidoId = pedidoResponse.id();
        assertNotNull(pedidoId, "O ID do pedido não deveria ser nulo");

        // Verificar se o pedido foi salvo no banco
        Optional<Pedido> pedidoSalvo = pedidoRepositorio.buscarPorId(pedidoId);
        assertTrue(pedidoSalvo.isPresent(), "O pedido deveria estar salvo no banco");
        assertEquals(StatusPedido.RECEBIDO, pedidoSalvo.get().getStatus(), "O status deveria ser RECEBIDO");
        assertEquals(2, pedidoSalvo.get().getItens().size(), "O pedido deveria ter 2 itens");
        assertEquals(cliente.getId(), pedidoSalvo.get().getCliente().getId(), "O cliente do pedido deveria ser o mesmo");

        // Listar pedidos e verificar se o novo pedido está na lista
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pedidoId))
                .andExpect(jsonPath("$[0].clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$[0].nomeCliente").value(cliente.getNome()))
                .andExpect(jsonPath("$[0].status").value("RECEBIDO"))
                .andExpect(jsonPath("$[0].valorTotal").value(57.80));
    }

    @Test
    @DisplayName("Deve realizar checkout de um pedido sem cliente identificado")
    void t2() throws Exception {
        // Criar pedido request sem cliente
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(produto1.getId(), 1)
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(null, itens);

        // Realizar checkout do pedido
        MvcResult resultado = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").isEmpty())
                .andExpect(jsonPath("$.nomeCliente").isEmpty())
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.valorTotal").value(25.90))
                .andReturn();

        // Extrair ID do pedido da resposta
        String respostaJson = resultado.getResponse().getContentAsString();
        PedidoResponseDTO pedidoResponse = objectMapper.readValue(respostaJson, PedidoResponseDTO.class);
        Long pedidoId = pedidoResponse.id();
        assertNotNull(pedidoId, "O ID do pedido não deveria ser nulo");

        // Verificar se o pedido foi salvo no banco
        Optional<Pedido> pedidoSalvo = pedidoRepositorio.buscarPorId(pedidoId);
        assertTrue(pedidoSalvo.isPresent(), "O pedido deveria estar salvo no banco");
        assertNull(pedidoSalvo.get().getCliente(), "O cliente do pedido deveria ser nulo");
        assertEquals(1, pedidoSalvo.get().getItens().size(), "O pedido deveria ter 1 item");
        assertEquals(produto1.getId(), pedidoSalvo.get().getItens().get(0).getProduto().getId(),
                "O produto do item deveria ser o correto");
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar checkout com CPF inexistente")
    void t3() throws Exception {
        // Criar pedido request com CPF inexistente
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(produto1.getId(), 1)
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO("99999999999", itens);

        // Executar e verificar erro
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar checkout com produto inexistente")
    void t4() throws Exception {
        // Criar pedido request com produto inexistente
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(999L, 1)
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(cliente.getCpf(), itens);

        // Executar e verificar erro
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar checkout com quantidade inválida")
    void t5() throws Exception {
        // Criar pedido request com quantidade zero
        List<ItemPedidoDTO> itens = Arrays.asList(
                new ItemPedidoDTO(produto1.getId(), 0)
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(cliente.getCpf(), itens);

        // Executar e verificar erro
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantidade deve ser maior que zero"));
    }
}