package br.com.lanchonete.autoatendimento.e2e;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.*;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FluxoCompletoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoGateway produtoGateway;

    private Produto lanche;
    private Produto bebida;
    private Produto acompanhamento;
    private Produto sobremesa;

    @BeforeEach
    void configurar() {
        // Criar e salvar produtos para o teste
        lanche = Produto.criar( "X-Tudo",
                "Hambúrguer completo com todos os ingredientes",
                new BigDecimal("32.90"),
                Categoria.LANCHE);
        lanche = produtoGateway.salvar(lanche);

        bebida = Produto.criar( "Refrigerante Cola",
                "Refrigerante cola 350ml",
                new BigDecimal("6.90"),
                Categoria.BEBIDA);
        bebida = produtoGateway.salvar(bebida);

        acompanhamento = Produto.criar("Batata Frita",
                "Porção de batata frita crocante",
                new BigDecimal("12.90"),
                Categoria.ACOMPANHAMENTO);
        acompanhamento = produtoGateway.salvar(acompanhamento);

        sobremesa = Produto.criar( "Sundae de Chocolate",
                "Sorvete com calda de chocolate e amendoim",
                new BigDecimal("9.90"),
                Categoria.SOBREMESA);
        sobremesa = produtoGateway.salvar(sobremesa);
    }

    @Test
    @DisplayName("Deve executar o fluxo completo do pedido - cadastro de cliente, busca produtos e realiza checkout")
    void t1() throws Exception {
        // ETAPA 1: Cadastrar um novo cliente
        String cpfCliente = "98765432100";
        ClienteRequestDTO novoCliente = new ClienteRequestDTO(
                "Maria dos Santos",
                cpfCliente,
                "maria.santos@email.com"
        );

        // Executar requisição para cadastro de cliente
        MvcResult resultadoCadastroCliente = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extrair resposta
        String respostaCadastroClienteJson = resultadoCadastroCliente.getResponse().getContentAsString();
        ClienteResponseDTO clienteResponse = objectMapper.readValue(respostaCadastroClienteJson, ClienteResponseDTO.class);

        // Verificar dados do cliente cadastrado
        assertNotNull(clienteResponse.id(), "O ID do cliente não deveria ser nulo");
        assertEquals(novoCliente.nome(), clienteResponse.nome());
        assertEquals(novoCliente.cpf(), clienteResponse.cpf());
        assertEquals(novoCliente.email(), clienteResponse.email());

        // ETAPA 2: Identificar o cliente pelo CPF para o pedido
        mockMvc.perform(get("/clientes/cpf/{cpf}", cpfCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteResponse.id()))
                .andExpect(jsonPath("$.nome").value(novoCliente.nome()))
                .andExpect(jsonPath("$.cpf").value(novoCliente.cpf()))
                .andExpect(jsonPath("$.email").value(novoCliente.email()))
                .andReturn();

        // ETAPA 3: Buscar produtos por categoria para montar o pedido

        // Buscar lanches
        mockMvc.perform(get("/produtos/categoria/{categoria}", "LANCHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].categoria").value("LANCHE"))
                .andReturn();


        // Buscar acompanhamentos
        mockMvc.perform(get("/produtos/categoria/{categoria}", "ACOMPANHAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].categoria").value("ACOMPANHAMENTO"))
                .andReturn();


        // Buscar bebidas
        mockMvc.perform(get("/produtos/categoria/{categoria}", "BEBIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].categoria").value("BEBIDA"))
                .andReturn();


        // Buscar sobremesas
        mockMvc.perform(get("/produtos/categoria/{categoria}", "SOBREMESA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].categoria").value("SOBREMESA"))
                .andReturn();



        // ETAPA 4: Montar um combo completo e realizar checkout
        List<ItemPedidoDTO> itensPedido = Arrays.asList(
                new ItemPedidoDTO(lanche.getId(), 1),        // 1 lanche
                new ItemPedidoDTO(bebida.getId(), 2),        // 2 bebidas
                new ItemPedidoDTO(acompanhamento.getId(), 1), // 1 acompanhamento
                new ItemPedidoDTO(sobremesa.getId(), 1)      // 1 sobremesa
        );

        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(cpfCliente, itensPedido);

        // Realizar checkout
        MvcResult resultadoCheckout = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clienteResponse.id()))
                .andExpect(jsonPath("$.nomeCliente").value(novoCliente.nome()))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.itens.length()").value(4))
                .andReturn();

        // Extrair dados do pedido criado
        String respostaCheckoutJson = resultadoCheckout.getResponse().getContentAsString();
        PedidoResponseDTO pedidoResponse = objectMapper.readValue(respostaCheckoutJson, PedidoResponseDTO.class);

        // Verificar dados do pedido
        assertNotNull(pedidoResponse.id(), "O ID do pedido não deveria ser nulo");
        assertEquals(StatusPedidoDTO.RECEBIDO, pedidoResponse.status());
        assertEquals(4, pedidoResponse.itens().size());

        // Calcular valor total esperado: 1 lanche + 2 bebidas + 1 acompanhamento + 1 sobremesa
        BigDecimal valorTotalEsperado = lanche.getPreco().getValor()
                .add(bebida.getPreco().getValor().multiply(new BigDecimal("2")))
                .add(acompanhamento.getPreco().getValor())
                .add(sobremesa.getPreco().getValor());

        assertEquals(valorTotalEsperado, pedidoResponse.valorTotal());

        // ETAPA 5: Listar pedidos e verificar se o pedido está na lista
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(pedidoResponse.id()))
                .andExpect(jsonPath("$[0].clienteId").value(clienteResponse.id()))
                .andExpect(jsonPath("$[0].nomeCliente").value(novoCliente.nome()))
                .andExpect(jsonPath("$[0].status").value("RECEBIDO"))
                .andExpect(jsonPath("$[0].itens.length()").value(4))
                .andReturn();
    }

    @Test
    @DisplayName("Deve executar o fluxo de pedido sem identificação do cliente")
    void t2() throws Exception {
        // ETAPA 1: Buscar produtos por categoria para montar o pedido

        // Buscar lanches
        mockMvc.perform(get("/produtos/categoria/{categoria}", "LANCHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].categoria").value("LANCHE"));

        // Buscar bebidas
        mockMvc.perform(get("/produtos/categoria/{categoria}", "BEBIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].categoria").value("BEBIDA"));

        // ETAPA 2: Montar um pedido simples sem identificação do cliente
        List<ItemPedidoDTO> itensPedido = Arrays.asList(
                new ItemPedidoDTO(lanche.getId(), 1),
                new ItemPedidoDTO(bebida.getId(), 1)
        );

        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(null, itensPedido);

        // Realizar checkout
        MvcResult resultadoCheckout = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").isEmpty())
                .andExpect(jsonPath("$.nomeCliente").isEmpty())
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andReturn();

        // Extrair dados do pedido criado
        String respostaCheckoutJson = resultadoCheckout.getResponse().getContentAsString();
        PedidoResponseDTO pedidoResponse = objectMapper.readValue(respostaCheckoutJson, PedidoResponseDTO.class);

        // Verificar dados do pedido
        assertNotNull(pedidoResponse.id(), "O ID do pedido não deveria ser nulo");
        assertNull(pedidoResponse.clienteId(), "O ID do cliente deveria ser nulo");
        assertNull(pedidoResponse.nomeCliente(), "O nome do cliente deveria ser nulo");
        assertEquals(StatusPedidoDTO.RECEBIDO, pedidoResponse.status());
        assertEquals(2, pedidoResponse.itens().size());

        // Calcular valor total esperado: 1 lanche + 1 bebida
        BigDecimal valorTotalEsperado = lanche.getPreco().getValor().add(bebida.getPreco().getValor());
        assertEquals(valorTotalEsperado, pedidoResponse.valorTotal());

        // ETAPA 3: Listar pedidos e verificar se o pedido está na lista
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(pedidoResponse.id()))
                .andExpect(jsonPath("$[0].clienteId").isEmpty())
                .andExpect(jsonPath("$[0].nomeCliente").isEmpty())
                .andExpect(jsonPath("$[0].status").value("RECEBIDO"))
                .andExpect(jsonPath("$[0].itens.length()").value(2));
    }

    @Test
    @DisplayName("Deve validar corretamente os erros no fluxo de pedido")
    void t3() throws Exception {
        // ETAPA 1: Tentar realizar checkout com cliente inexistente
        List<ItemPedidoDTO> itensValidos = Arrays.asList(
                new ItemPedidoDTO(lanche.getId(), 1),
                new ItemPedidoDTO(bebida.getId(), 1)
        );

        PedidoRequestDTO pedidoComClienteInexistente = new PedidoRequestDTO("11122233344", itensValidos);

        // Deve retornar erro 404 - cliente não encontrado
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoComClienteInexistente)))
                .andExpect(status().isNotFound());

        // ETAPA 2: Tentar realizar checkout com produto inexistente
        List<ItemPedidoDTO> itensComProdutoInvalido = List.of(
                new ItemPedidoDTO(999L, 1)
        );

        PedidoRequestDTO pedidoComProdutoInexistente = new PedidoRequestDTO(null, itensComProdutoInvalido);

        // Deve retornar erro 404 - produto não encontrado
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoComProdutoInexistente)))
                .andExpect(status().isNotFound());

        // ETAPA 3: Tentar realizar checkout sem itens
        List<ItemPedidoDTO> itensSemProdutos = Collections.emptyList();
        PedidoRequestDTO pedidoSemItens = new PedidoRequestDTO(null, itensSemProdutos);

        // Deve retornar erro 400 - pedido deve ter pelo menos um item
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoSemItens)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pedido deve conter pelo menos um item"));

        // ETAPA 4: Tentar realizar checkout com quantidade inválida
        List<ItemPedidoDTO> itensQuantidadeInvalida = List.of(
                new ItemPedidoDTO(lanche.getId(), 0)
        );

        PedidoRequestDTO pedidoQuantidadeInvalida = new PedidoRequestDTO(null, itensQuantidadeInvalida);

        // Deve retornar erro 400 - quantidade deve ser maior que zero
        mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoQuantidadeInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quantidade deve ser maior que zero"));
    }
}