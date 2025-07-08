package br.com.lanchonete.autoatendimento.adaptadores.persistencia;

import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteGateway;
import br.com.lanchonete.autoatendimento.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoGateway;
import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.Pedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
class PedidoGatewayJDBCTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PedidoGatewayJDBC pedidoRepositorio;

    private Cliente cliente;
    private Produto produtoLanche;
    private Produto produtoBebida;

    @BeforeEach
    void configurar() {

        // Criar instâncias dos repositórios necessários
        ClienteGateway clienteGateway = new ClienteGatewayJDBC(jdbcTemplate);
        ProdutoGateway produtoGateway = new ProdutoGatewayJDBC(jdbcTemplate);

        // Criar o repositório de pedido para teste
        pedidoRepositorio = new PedidoGatewayJDBC(jdbcTemplate, clienteGateway, produtoGateway);

        // Criar e salvar cliente de teste no banco
        cliente = Cliente.criar("João Silva", "joao@email.com","12345678901");
        cliente = clienteGateway.salvar(cliente);

        // Criar e salvar produtos de teste no banco
        produtoLanche = Produto.criar("X-Bacon", "Hambúrguer com bacon",
                new BigDecimal("25.90"), Categoria.LANCHE);
        produtoLanche = produtoGateway.salvar(produtoLanche);

        produtoBebida = Produto.criar("Refrigerante", "Refrigerante lata 350ml",
                new BigDecimal("6.00"), Categoria.BEBIDA);
        produtoBebida = produtoGateway.salvar(produtoBebida);
    }

    @Test
    @DisplayName("Deve salvar um pedido completo com sucesso")
    void t1() {
        // Criar um pedido com cliente
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());

        // Adicionar itens ao pedido
        ItemPedido item1 = ItemPedido.criar(
                Produto.reconstituir(produtoLanche.getId(), produtoLanche.getNome(),
                        produtoLanche.getDescricao(), produtoLanche.getPreco().getValor(), produtoLanche.getCategoria()),
                2
        );
        pedido.adicionarItem(item1);

        ItemPedido item2 = ItemPedido.criar(
                Produto.reconstituir(produtoBebida.getId(), produtoBebida.getNome(),
                        produtoBebida.getDescricao(), produtoBebida.getPreco().getValor(), produtoBebida.getCategoria()),
                1
        );
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
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());

        // Adicionar um item ao pedido
        ItemPedido item = ItemPedido.criar(
                produtoLanche,
                1
        );
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
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());

        ItemPedido item = ItemPedido.criar(
                Produto.reconstituir(produtoLanche.getId(), produtoLanche.getNome(),
                        produtoLanche.getDescricao(), produtoLanche.getPreco().getValor(), produtoLanche.getCategoria()),
                1
        );
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
    @DisplayName("Deve listar pedidos excluindo os finalizados")
    void t5() {
        // Criar e salvar pedidos com status diferentes
        LocalDateTime agora = LocalDateTime.now();
        
        // Pedido RECEBIDO (deve aparecer na lista)
        Pedido pedidoRecebido = Pedido.criar(cliente, StatusPedido.RECEBIDO, agora.minusHours(1));
        pedidoRecebido.setValorTotal(BigDecimal.TEN);
        Pedido pedidoRecebidoSalvo = pedidoRepositorio.salvar(pedidoRecebido);

        // Pedido FINALIZADO (não deve aparecer na lista)
        Pedido pedidoFinalizado = Pedido.criar(cliente, StatusPedido.FINALIZADO, agora);
        pedidoFinalizado.setValorTotal(BigDecimal.ONE);
        pedidoRepositorio.salvar(pedidoFinalizado);

        // Listar todos os pedidos
        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        // Verificações
        assertEquals(1, pedidos.size(), "Deve retornar apenas 1 pedido (excluindo FINALIZADO)");
        assertEquals(pedidoRecebidoSalvo.getId(), pedidos.get(0).getId(), "Deve retornar apenas o pedido RECEBIDO");
        assertEquals(StatusPedido.RECEBIDO, pedidos.get(0).getStatus(), "Status deve ser RECEBIDO");
        
        // Verificar que nenhum pedido finalizado foi retornado
        boolean temPedidoFinalizado = pedidos.stream()
                .anyMatch(p -> p.getStatus() == StatusPedido.FINALIZADO);
        assertFalse(temPedidoFinalizado, "Não deve retornar pedidos com status FINALIZADO");
    }

    @Test
    @DisplayName("Deve listar pedidos ordenados por status: PRONTO > EM_PREPARACAO > RECEBIDO")
    void t13() {
        // Criar pedidos com diferentes status
        LocalDateTime agora = LocalDateTime.now();
        
        // Pedido RECEBIDO (deve aparecer por último)
        Pedido pedidoRecebido = Pedido.criar(cliente, StatusPedido.RECEBIDO, agora.minusHours(3));
        pedidoRecebido.setValorTotal(new BigDecimal("10.00"));
        Pedido pedidoRecebidoSalvo = pedidoRepositorio.salvar(pedidoRecebido);

        // Pedido EM_PREPARACAO (deve aparecer no meio)
        Pedido pedidoEmPreparacao = Pedido.criar(cliente, StatusPedido.EM_PREPARACAO, agora.minusHours(2));
        pedidoEmPreparacao.setValorTotal(new BigDecimal("20.00"));
        Pedido pedidoEmPreparacaoSalvo = pedidoRepositorio.salvar(pedidoEmPreparacao);

        // Pedido PRONTO (deve aparecer primeiro)
        Pedido pedidoPronto = Pedido.criar(cliente, StatusPedido.PRONTO, agora.minusHours(1));
        pedidoPronto.setValorTotal(new BigDecimal("30.00"));
        Pedido pedidoProntoSalvo = pedidoRepositorio.salvar(pedidoPronto);

        // Listar todos os pedidos
        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        // Verificações
        assertEquals(3, pedidos.size(), "Deve retornar 3 pedidos");
        
        // Verificar ordenação por status
        assertEquals(StatusPedido.PRONTO, pedidos.get(0).getStatus(), 
                "Primeiro pedido deve ter status PRONTO");
        assertEquals(StatusPedido.EM_PREPARACAO, pedidos.get(1).getStatus(), 
                "Segundo pedido deve ter status EM_PREPARACAO");
        assertEquals(StatusPedido.RECEBIDO, pedidos.get(2).getStatus(), 
                "Terceiro pedido deve ter status RECEBIDO");
        
        // Verificar IDs dos pedidos
        assertEquals(pedidoProntoSalvo.getId(), pedidos.get(0).getId(), 
                "Primeiro deve ser o pedido PRONTO");
        assertEquals(pedidoEmPreparacaoSalvo.getId(), pedidos.get(1).getId(), 
                "Segundo deve ser o pedido EM_PREPARACAO");
        assertEquals(pedidoRecebidoSalvo.getId(), pedidos.get(2).getId(), 
                "Terceiro deve ser o pedido RECEBIDO");
    }

    @Test
    @DisplayName("Deve listar pedidos do mesmo status ordenados por data de criação (mais antigos primeiro)")
    void t14() {
        // Criar múltiplos pedidos com mesmo status mas datas diferentes
        LocalDateTime agora = LocalDateTime.now();
        
        // Pedido RECEBIDO mais recente
        Pedido pedidoRecente = Pedido.criar(cliente, StatusPedido.RECEBIDO, agora.minusMinutes(30));
        pedidoRecente.setValorTotal(new BigDecimal("10.00"));
        Pedido pedidoRecenteSalvo = pedidoRepositorio.salvar(pedidoRecente);

        // Pedido RECEBIDO mais antigo
        Pedido pedidoAntigo = Pedido.criar(cliente, StatusPedido.RECEBIDO, agora.minusHours(2));
        pedidoAntigo.setValorTotal(new BigDecimal("20.00"));
        Pedido pedidoAntigoSalvo = pedidoRepositorio.salvar(pedidoAntigo);

        // Listar todos os pedidos
        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        // Verificações
        assertEquals(2, pedidos.size(), "Deve retornar 2 pedidos");
        
        // Verificar ordenação por data (mais antigos primeiro)
        assertTrue(pedidos.get(0).getDataCriacao().isBefore(pedidos.get(1).getDataCriacao()),
                "Primeiro pedido deve ser mais antigo que o segundo");
        assertEquals(pedidoAntigoSalvo.getId(), pedidos.get(0).getId(), 
                "Primeiro pedido deve ser o mais antigo");
        assertEquals(pedidoRecenteSalvo.getId(), pedidos.get(1).getId(), 
                "Segundo pedido deve ser o mais recente");
    }

    @Test
    @DisplayName("Deve atualizar o status de um pedido")
    void t6() {
        // Criar e salvar um pedido para o teste
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido.setValorTotal(BigDecimal.TEN);
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
        assertThrows(RecursoNaoEncontradoException.class,
                () -> pedidoRepositorio.atualizarStatus(999L, StatusPedido.FINALIZADO),
                "Deve lançar exceção para pedido inexistente");
    }

    @Test
    @DisplayName("Deve salvar pedido com status de pagamento PENDENTE por padrão")
    void t8() {
        // Criar um pedido
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());

        // Adicionar um item ao pedido
        ItemPedido item = ItemPedido.criar(produtoLanche, 1);
        pedido.adicionarItem(item);

        // Salvar o pedido
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Buscar o pedido para verificar se foi salvo corretamente
        Optional<Pedido> pedidoEncontrado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        // Verificações
        assertTrue(pedidoEncontrado.isPresent(), "O pedido deve ser encontrado");
        assertEquals(StatusPagamento.PENDENTE, pedidoEncontrado.get().getStatusPagamento(),
                "Status de pagamento deve ser PENDENTE por padrão");
    }

    @Test
    @DisplayName("Deve salvar e recuperar pedido com status de pagamento APROVADO")
    void t9() {
        // Criar um pedido
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());

        // Adicionar um item ao pedido
        ItemPedido item = ItemPedido.criar(produtoLanche, 1);
        pedido.adicionarItem(item);

        // Alterar status de pagamento para APROVADO
        pedido.aprovarPagamento();

        // Salvar o pedido
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Buscar o pedido para verificar se foi salvo corretamente
        Optional<Pedido> pedidoEncontrado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        // Verificações
        assertTrue(pedidoEncontrado.isPresent(), "O pedido deve ser encontrado");
        assertEquals(StatusPagamento.APROVADO, pedidoEncontrado.get().getStatusPagamento(),
                "Status de pagamento deve ser APROVADO");
    }

    @Test
    @DisplayName("Deve salvar e recuperar pedido com status de pagamento REJEITADO")
    void t10() {
        // Criar um pedido
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());

        // Adicionar um item ao pedido
        ItemPedido item = ItemPedido.criar(produtoLanche, 1);
        pedido.adicionarItem(item);

        // Alterar status de pagamento para REJEITADO
        pedido.rejeitarPagamento();

        // Salvar o pedido
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Buscar o pedido para verificar se foi salvo corretamente
        Optional<Pedido> pedidoEncontrado = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        // Verificações
        assertTrue(pedidoEncontrado.isPresent(), "O pedido deve ser encontrado");
        assertEquals(StatusPagamento.REJEITADO, pedidoEncontrado.get().getStatusPagamento(),
                "Status de pagamento deve ser REJEITADO");
    }

    @Test
    @DisplayName("Deve atualizar status de pagamento de um pedido existente")
    void t11() {
        // Criar e salvar um pedido
        Pedido pedido = Pedido.criar(cliente, StatusPedido.RECEBIDO, LocalDateTime.now());
        ItemPedido item = ItemPedido.criar(produtoLanche, 1);
        pedido.adicionarItem(item);
        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        // Verificar que foi salvo com status PENDENTE
        Optional<Pedido> pedidoAntes = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());
        assertTrue(pedidoAntes.isPresent());
        assertEquals(StatusPagamento.PENDENTE, pedidoAntes.get().getStatusPagamento());

        // Atualizar status de pagamento para APROVADO
        pedidoRepositorio.atualizarStatusPagamento(pedidoSalvo.getId(), StatusPagamento.APROVADO);

        // Buscar novamente para verificar a atualização
        Optional<Pedido> pedidoDepois = pedidoRepositorio.buscarPorId(pedidoSalvo.getId());

        // Verificações
        assertTrue(pedidoDepois.isPresent(), "O pedido deve ser encontrado");
        assertEquals(StatusPagamento.APROVADO, pedidoDepois.get().getStatusPagamento(),
                "Status de pagamento deve ser atualizado para APROVADO");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar status de pagamento de pedido inexistente")
    void t12() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> pedidoRepositorio.atualizarStatusPagamento(999L, StatusPagamento.APROVADO),
                "Deve lançar exceção para pedido inexistente");
    }
}