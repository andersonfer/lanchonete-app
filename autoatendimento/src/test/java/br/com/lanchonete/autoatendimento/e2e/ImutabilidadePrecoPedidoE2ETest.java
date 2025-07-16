package br.com.lanchonete.autoatendimento.e2e;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ItemPedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoGateway;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import br.com.lanchonete.autoatendimento.dominio.modelo.shared.Preco;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImutabilidadePrecoPedidoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteGateway clienteGateway;

    @Autowired
    private ProdutoGateway produtoGateway;

    @Autowired
    private PedidoGateway pedidoGateway;

    private Cliente cliente;
    private Produto produto;
    private final BigDecimal precoInicial = new BigDecimal("25.90");
    private final BigDecimal precoAlterado = new BigDecimal("32.50");

    @BeforeEach
    void configurar() {
        // Criar e salvar cliente para o teste
        cliente = Cliente.criar("João Silva", "joao@email.com","12345678901");
        cliente = clienteGateway.salvar(cliente);

        // Criar e salvar produto com preço inicial
        produto = Produto.criar(
                "X-Bacon Especial",
                "Hambúrguer com bacon e queijo especial",
                precoInicial,
                Categoria.LANCHE);
        produto = produtoGateway.salvar(produto);
    }

    @Test
    @DisplayName("Deve manter o valor original do pedido mesmo após alteração do preço do produto")
    void t1() throws Exception {
        // ETAPA 1: Realizar checkout de um pedido com o preço inicial do produto
        List<ItemPedidoRequestDTO> itens = Collections.singletonList(
                new ItemPedidoRequestDTO(produto.getId(), 2) // 2 unidades do produto
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(cliente.getCpf().getValor(), itens);

        // Realizar checkout
        MvcResult resultadoCheckout = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andReturn();

        // Extrair dados do pedido criado
        String respostaCheckoutJson = resultadoCheckout.getResponse().getContentAsString();
        PedidoResponseDTO pedidoResponse = objectMapper.readValue(respostaCheckoutJson, PedidoResponseDTO.class);

        // Verificar valor total do pedido (preço inicial * 2 unidades)
        BigDecimal valorTotalEsperado = precoInicial.multiply(new BigDecimal("2"));
        assertEquals(valorTotalEsperado, pedidoResponse.valorTotal());

        // Guardar o ID do pedido para consulta posterior
        Long pedidoId = pedidoResponse.id();

        // ETAPA 2: Alterar o preço do produto (simulando uma atualização de preço na lanchonete)
        Produto produtoParaAtualizar = produtoGateway.buscarPorId(produto.getId()).orElseThrow();
        produtoParaAtualizar.setPreco(new Preco(precoAlterado));

        // Atualizar o produto com o novo preço
        ProdutoRequestDTO produtoRequest = new ProdutoRequestDTO(
                produtoParaAtualizar.getNome(),
                produtoParaAtualizar.getDescricao(),
                produtoParaAtualizar.getPreco().getValor(),
                produtoParaAtualizar.getCategoria());
        
        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.getId()))
                .andExpect(jsonPath("$.preco").value(precoAlterado.doubleValue()))
                .andReturn();

        // ETAPA 3: Verificar se o produto foi atualizado corretamente no banco
        Optional<Produto> produtoAtualizado = produtoGateway.buscarPorId(produto.getId());
        assertTrue(produtoAtualizado.isPresent());
        assertEquals(precoAlterado, produtoAtualizado.get().getPreco().getValor());

        // ETAPA 4: Recuperar o pedido e verificar se o valor total permanece inalterado
        Optional<Pedido> pedidoPersistido = pedidoGateway.buscarPorId(pedidoId);
        assertTrue(pedidoPersistido.isPresent());

        Pedido pedido = pedidoPersistido.get();

        // Verificar se o valor total do pedido permanece o mesmo (baseado no preço original)
        assertEquals(valorTotalEsperado, pedido.getValorTotal());

        // Verificar se o valor unitário do item do pedido permanece o mesmo (preço original)
        ItemPedido itemPedido = pedido.getItens().get(0);
        assertEquals(precoInicial, itemPedido.getValorUnitario());

        // ETAPA 5: Buscar o pedido pela API e verificar se o valor permanece inalterado
        MvcResult resultadoListaPedidos = mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String respostaListaPedidosJson = resultadoListaPedidos.getResponse().getContentAsString();
        List<PedidoResponseDTO> pedidos = objectMapper.readValue(
                respostaListaPedidosJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PedidoResponseDTO.class)
        );

        // Encontrar o pedido na lista
        Optional<PedidoResponseDTO> pedidoNaLista = pedidos.stream()
                .filter(p -> p.id().equals(pedidoId))
                .findFirst();

        assertTrue(pedidoNaLista.isPresent());

        // Verificar valor total do pedido na API
        assertEquals(valorTotalEsperado, pedidoNaLista.get().valorTotal());

        // Verificar preço unitário do item na API
        assertEquals(precoInicial, pedidoNaLista.get().itens().get(0).valorUnitario());
    }

    @Test
    @DisplayName("Deve registrar o valor correto em múltiplos pedidos feitos antes e depois da alteração de preço")
    void t2() throws Exception {
        // ETAPA 1: Realizar o primeiro pedido com o preço inicial
        List<ItemPedidoRequestDTO> itens = Collections.singletonList(
                new ItemPedidoRequestDTO(produto.getId(), 1)
        );
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(cliente.getCpf().getValor(), itens);

        // Realizar checkout do primeiro pedido
        MvcResult resultadoPrimeiroPedido = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(precoInicial.doubleValue()))
                .andReturn();

        String respostaPrimeiroPedidoJson = resultadoPrimeiroPedido.getResponse().getContentAsString();
        PedidoResponseDTO primeiroPedidoResponse = objectMapper.readValue(respostaPrimeiroPedidoJson, PedidoResponseDTO.class);

        // ETAPA 2: Alterar o preço do produto
        Produto produtoParaAtualizar = produtoGateway.buscarPorId(produto.getId()).orElseThrow();
        produtoParaAtualizar.setPreco(new Preco(precoAlterado));

        ProdutoRequestDTO produtoRequestSegundo = new ProdutoRequestDTO(
                produtoParaAtualizar.getNome(),
                produtoParaAtualizar.getDescricao(),
                produtoParaAtualizar.getPreco().getValor(),
                produtoParaAtualizar.getCategoria());
        
        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequestSegundo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(precoAlterado.doubleValue()));

        // ETAPA 3: Realizar um segundo pedido com o novo preço
        MvcResult resultadoSegundoPedido = mockMvc.perform(post("/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(precoAlterado.doubleValue()))
                .andReturn();

        String respostaSegundoPedidoJson = resultadoSegundoPedido.getResponse().getContentAsString();
        PedidoResponseDTO segundoPedidoResponse = objectMapper.readValue(respostaSegundoPedidoJson, PedidoResponseDTO.class);

        // ETAPA 4: Verificar a lista de pedidos e confirmar os valores corretos para ambos
        MvcResult resultadoListaPedidos = mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String respostaListaPedidosJson = resultadoListaPedidos.getResponse().getContentAsString();
        List<PedidoResponseDTO> pedidos = objectMapper.readValue(
                respostaListaPedidosJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PedidoResponseDTO.class)
        );

        // Encontrar ambos pedidos na lista
        Optional<PedidoResponseDTO> primeiroPedidoNaLista = pedidos.stream()
                .filter(p -> p.id().equals(primeiroPedidoResponse.id()))
                .findFirst();

        Optional<PedidoResponseDTO> segundoPedidoNaLista = pedidos.stream()
                .filter(p -> p.id().equals(segundoPedidoResponse.id()))
                .findFirst();

        assertTrue(primeiroPedidoNaLista.isPresent());
        assertTrue(segundoPedidoNaLista.isPresent());

        // Verificar valor total de cada pedido na API
        // O primeiro pedido deve manter o preço inicial
        assertEquals(precoInicial, primeiroPedidoNaLista.get().valorTotal());
        assertEquals(precoInicial, primeiroPedidoNaLista.get().itens().get(0).valorUnitario());

        // O segundo pedido deve ter o novo preço
        assertEquals(precoAlterado, segundoPedidoNaLista.get().valorTotal());
        assertEquals(precoAlterado, segundoPedidoNaLista.get().itens().get(0).valorUnitario());

        // Verificar diretamente no banco de dados
        Optional<Pedido> primeiroPedidoPersistido = pedidoGateway.buscarPorId(primeiroPedidoResponse.id());
        Optional<Pedido> segundoPedidoPersistido = pedidoGateway.buscarPorId(segundoPedidoResponse.id());

        assertTrue(primeiroPedidoPersistido.isPresent());
        assertTrue(segundoPedidoPersistido.isPresent());

        assertEquals(precoInicial, primeiroPedidoPersistido.get().getValorTotal());
        assertEquals(precoInicial, primeiroPedidoPersistido.get().getItens().get(0).getValorUnitario());

        assertEquals(precoAlterado, segundoPedidoPersistido.get().getValorTotal());
        assertEquals(precoAlterado, segundoPedidoPersistido.get().getItens().get(0).getValorUnitario());
    }
}