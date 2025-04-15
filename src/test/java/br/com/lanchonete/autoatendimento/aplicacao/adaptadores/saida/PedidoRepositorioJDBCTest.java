package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.saida;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.RegistroNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class PedidoRepositorioJDBCTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PedidoRepositorioJDBC pedidoRepositorio;

    private Cliente cliente;
    private Produto produtoLanche;
    private Produto produtoBebida;

    @BeforeEach
    void configurar() {

        // Criar instâncias dos repositórios necessários
        ClienteRepositorio clienteRepositorio = new ClienteRepositorioJDBC(jdbcTemplate);
        ProdutoRepositorio produtoRepositorio = new ProdutoRepositorioJDBC(jdbcTemplate);

        // Criar o repositório de pedido para teste
        pedidoRepositorio = new PedidoRepositorioJDBC(jdbcTemplate, clienteRepositorio, produtoRepositorio);

        // Criar e salvar cliente de teste no banco
        cliente = Cliente.builder()
                .nome("João Silva")
                .cpf("12345678901")
                .email("joao@email.com")
                .build();
        cliente = clienteRepositorio.salvar(cliente);

        // Criar e salvar produtos de teste no banco
        produtoLanche = Produto.builder()
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon")
                .preco(new BigDecimal("25.90"))
                .categoria(Categoria.LANCHE)
                .build();
        produtoLanche = produtoRepositorio.salvar(produtoLanche);

        produtoBebida = Produto.builder()
                .nome("Refrigerante")
                .descricao("Refrigerante lata 350ml")
                .preco(new BigDecimal("6.00"))
                .categoria(Categoria.BEBIDA)
                .build();
        produtoBebida = produtoRepositorio.salvar(produtoBebida);
    }

    @Test
    @DisplayName("Deve salvar um pedido completo com sucesso")
    void t1() {
        // Criar um pedido com cliente
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(LocalDateTime.now())
                .itens(new ArrayList<>())
                .build();

        // Adicionar itens ao pedido
        ItemPedido item1 = ItemPedido.builder()
                .produto(produtoLanche)
                .quantidade(2)
                .valorUnitario(produtoLanche.getPreco())
                .build();
        item1.calcularValorTotal();
        pedido.adicionarItem(item1);

        ItemPedido item2 = ItemPedido.builder()
                .produto(produtoBebida)
                .quantidade(1)
                .valorUnitario(produtoBebida.getPreco())
                .build();
        item2.calcularValorTotal();
        pedido.adicionarItem(item2);

        // Salvar o pedido
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Verificações
        assertNotNull(pedidoSalvo.getId(), "O ID do pedido deve ser gerado");
        assertEquals(2, pedidoSalvo.getItens().size(), "O pedido deve ter 2 itens");
        assertNotNull(pedidoSalvo.getItens().get(0).getId(), "O ID do item 1 deve ser gerado");
        assertNotNull(pedidoSalvo.getItens().get(1).getId(), "O ID do item 2 deve ser gerado");
        assertEquals(new BigDecimal("57.80"), pedidoSalvo.getValorTotal(),
                "O valor total deve ser 2 * 25.90 + 1 * 6.00 = 57.80");
    }

    // Os outros testes permanecem iguais...

    @Test
    @DisplayName("Deve salvar um pedido sem cliente identificado")
    void t2() {
        // Criar um pedido sem cliente (cliente não identificado)
        Pedido pedido = Pedido.builder()
                .cliente(null)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(LocalDateTime.now())
                .itens(new ArrayList<>())
                .build();

        // Adicionar um item ao pedido
        ItemPedido item = ItemPedido.builder()
                .produto(produtoLanche)
                .quantidade(1)
                .valorUnitario(produtoLanche.getPreco())
                .build();
        item.calcularValorTotal();
        pedido.adicionarItem(item);

        // Salvar o pedido
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Verificações
        assertNotNull(pedidoSalvo.getId(), "O ID do pedido deve ser gerado");
        assertNull(pedidoSalvo.getCliente(), "O cliente deve ser nulo");
        assertEquals(1, pedidoSalvo.getItens().size(), "O pedido deve ter 1 item");
        assertEquals(new BigDecimal("25.90"), pedidoSalvo.getValorTotal(),
                "O valor total deve ser igual ao preço do produto");
    }

    @Test
    @DisplayName("Deve buscar um pedido por ID com seus itens")
    void t3() {
        // Criar e salvar um pedido para o teste
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(LocalDateTime.now())
                .itens(new ArrayList<>())
                .build();

        ItemPedido item = ItemPedido.builder()
                .produto(produtoLanche)
                .quantidade(1)
                .valorUnitario(produtoLanche.getPreco())
                .build();
        item.calcularValorTotal();
        pedido.adicionarItem(item);

        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Buscar o pedido pelo ID
        Optional<Pedido> pedidoEncontrado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        // Verificações
        assertTrue(pedidoEncontrado.isPresent(), "O pedido deve ser encontrado");
        assertEquals(pedidoSalvo.getId(), pedidoEncontrado.get().getId(), "O ID deve ser o mesmo");
        assertEquals(1, pedidoEncontrado.get().getItens().size(), "O pedido deve ter 1 item");
        assertEquals(StatusPedido.RECEBIDO, pedidoEncontrado.get().getStatus(), "O status deve ser RECEBIDO");
        assertEquals(cliente.getId(), pedidoEncontrado.get().getCliente().getId(), "O cliente deve ser o mesmo");
        assertEquals(produtoLanche.getId(), pedidoEncontrado.get().getItens().get(0).getProduto().getId(),
                "O produto do item deve ser o mesmo");
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar ID inexistente")
    void t4() {
        Optional<Pedido> pedidoNaoExistente = pedidoRepositorio.buscarPorId(999L);
        assertTrue(pedidoNaoExistente.isEmpty(), "Deve retornar vazio para ID inexistente");
    }

    @Test
    @DisplayName("Deve listar todos os pedidos ordenados por data de criação decrescente")
    void t5() {

        // Criar e salvar dois pedidos com datas diferentes
        LocalDateTime dataAnterior = LocalDateTime.now().minusHours(1);
        Pedido pedidoAntigo = Pedido.builder()
                .cliente(null)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(dataAnterior)
                .itens(new ArrayList<>())
                .valorTotal(BigDecimal.TEN)
                .build();
        pedidoRepositorio.salvar(pedidoAntigo);

        LocalDateTime dataMaisRecente = LocalDateTime.now();
        Pedido pedidoRecente = Pedido.builder()
                .cliente(cliente)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(dataMaisRecente)
                .itens(new ArrayList<>())
                .valorTotal(BigDecimal.ONE)
                .build();
        pedidoRepositorio.salvar(pedidoRecente);

        // Listar todos os pedidos
        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        // Verificações
        assertEquals(2, pedidos.size(), "Deve retornar 2 pedidos");
        // O pedido mais recente deve vir primeiro devido à ordenação
        assertTrue(pedidos.get(0).getDataCriacao().isAfter(pedidos.get(1).getDataCriacao()),
                "O pedido mais recente deve estar em primeiro na lista");
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    void t6() {
        // Criar e salvar um pedido para o teste
        Pedido pedido = Pedido.builder()
                .cliente(null)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(LocalDateTime.now())
                .itens(new ArrayList<>())
                .valorTotal(BigDecimal.TEN)
                .build();
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Atualizar o status
        pedidoRepositorio.atualizarStatus(pedidoSalvo.getId(), StatusPedido.FINALIZADO);

        // Buscar o pedido para verificar o status
        Optional<Pedido> pedidoAtualizado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        // Verificação
        assertTrue(pedidoAtualizado.isPresent(), "O pedido deve ser encontrado");
        assertEquals(StatusPedido.FINALIZADO, pedidoAtualizado.get().getStatus(),
                "O status deve ser atualizado para FINALIZADO");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar status de pedido inexistente")
    void t7() {
        assertThrows(RegistroNaoEncontradoException.class,
                () -> pedidoRepositorio.atualizarStatus(999L, StatusPedido.FINALIZADO),
                "Deve lançar exceção para pedido inexistente");
    }
}