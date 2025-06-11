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
        ItemPedido item = new ItemPedido(
                produto,
                3,
                produto.getPreco().getValor()
        );

        // Verificar se o cálculo está correto
        assertEquals(new BigDecimal("77.70"), item.getValorTotal(),
                "O valor total deve ser 3 * 25.90 = 77.70");
    }

    @Test
    @DisplayName("Deve retornar zero quando quantidade for zero")
    void t2() {
        // Criar um produto para o teste

        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Criar o item de pedido com quantidade zero
        ItemPedido item = new ItemPedido(
                produto,
                0,
                produto.getPreco().getValor()
        );

        // Verificar se o valor total é zero
        assertEquals(BigDecimal.ZERO, item.getValorTotal(),
                "O valor total deve ser zero quando a quantidade é zero");
    }

    @Test
    @DisplayName("Deve retornar zero quando valor unitário for nulo")
    void t3() {
        // Criar um produto para o teste
        Produto produto = Produto.criar("X-Bacon", null, new BigDecimal("25.90"), Categoria.LANCHE);

        // Criar o item de pedido com valor unitário nulo
        ItemPedido item = new ItemPedido(
                produto,
                2,
                null
        );

        // Verificar se o valor total é zero
        assertEquals(BigDecimal.ZERO, item.getValorTotal(),
                "O valor total deve ser zero quando o valor unitário é nulo");
    }
}