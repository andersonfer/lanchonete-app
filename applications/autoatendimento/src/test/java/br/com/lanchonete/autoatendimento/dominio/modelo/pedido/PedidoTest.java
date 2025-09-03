package br.com.lanchonete.autoatendimento.dominio.modelo.pedido;

import br.com.lanchonete.autoatendimento.dominio.modelo.cliente.Cliente;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Produto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    @DisplayName("Deve calcular valor total do pedido corretamente")
    void t1() {
        // Cria um produto
        Produto produto = Produto.criar(
                "X-Bacon",
                null,
                new BigDecimal("25.90"),
                Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Cria e adicionar itens ao pedido
        ItemPedido item1 = ItemPedido.criar(
                produto,
                2
        );

        pedido.adicionarItem(item1);

        // Verificar o cálculo do valor total
        assertEquals(new BigDecimal("51.80"), pedido.getValorTotal(),
                "O valor total deve ser 2 * 25.90 = 51.80");
    }

    @Test
    @DisplayName("Deve criar um pedido válido com sucesso")
    void t2() {
        // Cria cliente e produto para o teste
        Cliente cliente = Cliente.reconstituir(1L, "João Silva", "joao@email.com", "12345678901");
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria pedido usando o método de fábrica
        Pedido pedido = Pedido.criar(
                cliente,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(
                produto,
                1
        );
        pedido.adicionarItem(item);

        // Valida o pedido
        assertDoesNotThrow(
                pedido::validar,
                "Não deve lançar exceção para um pedido válido"
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar pedido sem itens")
    void t3() {
        // Cria pedido sem itens
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Tenta validar o pedido sem itens
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                pedido::validar,
                "Deve lançar exceção para pedido sem itens"
        );

        assertEquals("Pedido deve conter pelo menos um item", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar pedido com item inválido")
    void t4() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Cria item com quantidade inválida usando reconstituir (dados inconsistentes)
        ItemPedido item = ItemPedido.reconstituir(
                null, // Sem ID
                null, // Sem pedido ainda
                produto,
                0, // Quantidade inválida
                produto.getPreco().getValor(),
                BigDecimal.ZERO
        );

        // Adiciona o item manualmente para evitar validação automática
        if (pedido.getItens() == null) {
            pedido.setItens(new ArrayList<>());
        }
        pedido.getItens().add(item);
        item.setPedido(pedido);

        // Tenta validar o pedido com item inválido
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                pedido::validar,
                "Deve lançar exceção para pedido com item inválido"
        );

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar pedido com status nulo")
    void t5() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido com status nulo
        Pedido pedido = Pedido.criar(null, null, LocalDateTime.now());

        // Adiciona um item válido ao pedido
        ItemPedido item = ItemPedido.criar(
                produto,
                1
        );
        pedido.adicionarItem(item);

        // Tenta validar o pedido com status nulo
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                pedido::validar,
                "Deve lançar exceção para pedido com status nulo"
        );

        assertEquals("Status do pedido é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar pedido com data de criação nula")
    void t6() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido com data de criação nula
        Pedido pedido = Pedido.criar(null, StatusPedido.RECEBIDO, null);

        // Adiciona um item válido ao pedido
        ItemPedido item = ItemPedido.criar(
                produto,
                1
        );
        pedido.adicionarItem(item);

        // Tenta validar o pedido com data de criação nula
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                pedido::validar,
                "Deve lançar exceção para pedido com data de criação nula"
        );

        assertEquals("Data de criação é obrigatória", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve criar pedido com status de pagamento PENDENTE por padrão")
    void t7() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Verifica se o status de pagamento é PENDENTE por padrão
        assertEquals(StatusPagamento.PENDENTE, pedido.getStatusPagamento(),
                "Status de pagamento deve ser PENDENTE por padrão");
    }

    @Test
    @DisplayName("Deve permitir alterar status de pagamento para APROVADO")
    void t8() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Altera o status de pagamento para APROVADO
        pedido.aprovarPagamento();

        // Verifica se o status foi alterado
        assertEquals(StatusPagamento.APROVADO, pedido.getStatusPagamento(),
                "Status de pagamento deve ser APROVADO após aprovação");
    }

    @Test
    @DisplayName("Deve permitir alterar status de pagamento para REJEITADO")
    void t9() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Altera o status de pagamento para REJEITADO
        pedido.rejeitarPagamento();

        // Verifica se o status foi alterado
        assertEquals(StatusPagamento.REJEITADO, pedido.getStatusPagamento(),
                "Status de pagamento deve ser REJEITADO após rejeição");
    }

    @Test
    @DisplayName("Deve verificar se pagamento está pendente")
    void t10() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Verifica se o pagamento está pendente
        assertTrue(pedido.isPagamentoPendente(),
                "Pagamento deve estar pendente inicialmente");

        // Aprova o pagamento
        pedido.aprovarPagamento();

        // Verifica se o pagamento não está mais pendente
        assertFalse(pedido.isPagamentoPendente(),
                "Pagamento não deve estar pendente após aprovação");
    }

    @Test
    @DisplayName("Deve verificar se pagamento foi aprovado")
    void t11() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Verifica se o pagamento não está aprovado inicialmente
        assertFalse(pedido.isPagamentoAprovado(),
                "Pagamento não deve estar aprovado inicialmente");

        // Aprova o pagamento
        pedido.aprovarPagamento();

        // Verifica se o pagamento está aprovado
        assertTrue(pedido.isPagamentoAprovado(),
                "Pagamento deve estar aprovado após aprovação");
    }

    @Test
    @DisplayName("Deve verificar se pagamento foi rejeitado")
    void t12() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Verifica se o pagamento não está rejeitado inicialmente
        assertFalse(pedido.isPagamentoRejeitado(),
                "Pagamento não deve estar rejeitado inicialmente");

        // Rejeita o pagamento
        pedido.rejeitarPagamento();

        // Verifica se o pagamento está rejeitado
        assertTrue(pedido.isPagamentoRejeitado(),
                "Pagamento deve estar rejeitado após rejeição");
    }

    @Test
    @DisplayName("Deve verificar se pagamento foi processado")
    void t13() {
        // Cria um produto
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria o pedido
        Pedido pedido = Pedido.criar(
                null,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.criar(produto, 1);
        pedido.adicionarItem(item);

        // Verifica se o pagamento não foi processado inicialmente
        assertFalse(pedido.isPagamentoProcessado(),
                "Pagamento não deve estar processado inicialmente");

        // Aprova o pagamento
        pedido.aprovarPagamento();

        // Verifica se o pagamento foi processado
        assertTrue(pedido.isPagamentoProcessado(),
                "Pagamento deve estar processado após aprovação");

        // Cria outro pedido e rejeita o pagamento
        Pedido pedido2 = Pedido.criar(null, StatusPedido.RECEBIDO, LocalDateTime.now());
        pedido2.adicionarItem(ItemPedido.criar(produto, 1));
        pedido2.rejeitarPagamento();

        // Verifica se o pagamento rejeitado também é considerado processado
        assertTrue(pedido2.isPagamentoProcessado(),
                "Pagamento rejeitado também deve ser considerado processado");
    }
}