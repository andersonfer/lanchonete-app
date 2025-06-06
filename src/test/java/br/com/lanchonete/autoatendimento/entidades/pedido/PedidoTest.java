package br.com.lanchonete.autoatendimento.entidades.pedido;

import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
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
        ItemPedido item1 = ItemPedido.builder()
                .produto(produto)
                .quantidade(2)
                .valorUnitario(produto.getPreco())
                .build();
        item1.calcularValorTotal();

        pedido.adicionarItem(item1);

        // Verificar o cálculo do valor total
        assertEquals(new BigDecimal("51.80"), pedido.getValorTotal(),
                "O valor total deve ser 2 * 25.90 = 51.80");
    }

    @Test
    @DisplayName("Deve criar um pedido válido com sucesso")
    void t2() {
        // Cria cliente e produto para o teste
        Cliente cliente = Cliente.criarSemValidacao(1L, "João Silva", "joao@email.com", "12345678901");
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Cria pedido usando o método de fábrica
        Pedido pedido = Pedido.criar(
                cliente,
                StatusPedido.RECEBIDO,
                LocalDateTime.now()
        );

        // Adiciona um item ao pedido
        ItemPedido item = ItemPedido.builder()
                .produto(produto)
                .quantidade(1)
                .valorUnitario(produto.getPreco())
                .build();
        item.calcularValorTotal();
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

        // Cria item com quantidade inválida
        ItemPedido item = ItemPedido.builder()
                .produto(produto)
                .quantidade(0) // Quantidade inválida
                .valorUnitario(produto.getPreco())
                .build();

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
        ItemPedido item = ItemPedido.builder()
                .produto(produto)
                .quantidade(1)
                .valorUnitario(produto.getPreco())
                .build();
        item.calcularValorTotal();
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
        ItemPedido item = ItemPedido.builder()
                .produto(produto)
                .quantidade(1)
                .valorUnitario(produto.getPreco())
                .build();
        item.calcularValorTotal();
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
}