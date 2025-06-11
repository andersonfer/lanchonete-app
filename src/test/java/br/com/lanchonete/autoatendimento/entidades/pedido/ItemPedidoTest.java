package br.com.lanchonete.autoatendimento.entidades.pedido;

import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import br.com.lanchonete.autoatendimento.entidades.shared.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    @DisplayName("Deve calcular valor total do item corretamente")
    void t1() {
        // Criar um produto para o teste
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Criar o item de pedido
        ItemPedido item = ItemPedido.criar(
                produto,
                3
        );

        // Verificar se o cálculo está correto
        assertEquals(new BigDecimal("77.70"), item.getValorTotal(),
                "O valor total deve ser 3 * 25.90 = 77.70");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar item com quantidade zero")
    void t2() {
        // Criar um produto para o teste
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Tentar criar item com quantidade zero deve lançar exceção
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ItemPedido.criar(produto, 0),
                "Deve lançar exceção para quantidade zero"
        );

        assertEquals("Quantidade deve ser maior que zero", exception.getMessage(),
                "Mensagem de erro incorreta");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar item com produto nulo")
    void t3() {
        // Tentar criar item com produto nulo deve lançar exceção
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ItemPedido.criar(null, 2),
                "Deve lançar exceção para produto nulo"
        );

        assertEquals("Produto é obrigatório", exception.getMessage(),
                "Mensagem de erro incorreta");
    }
}