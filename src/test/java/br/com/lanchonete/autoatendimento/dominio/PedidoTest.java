package br.com.lanchonete.autoatendimento.dominio;

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
        Produto produto = Produto.builder()
                .id(1L)
                .nome("X-Bacon")
                .preco(new BigDecimal("25.90"))
                .categoria(Categoria.LANCHE)
                .build();

        // Cria o pedido
        Pedido pedido = Pedido.builder()
                .dataCriacao(LocalDateTime.now())
                .status(StatusPedido.RECEBIDO)
                .itens(new ArrayList<>())
                .build();

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
}