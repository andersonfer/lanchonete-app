package br.com.lanchonete.pedidos.bdd.hooks;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseHooks {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setupDatabase() {
        // Limpar dados
        jdbcTemplate.execute("DELETE FROM itens_pedido");
        jdbcTemplate.execute("DELETE FROM pedidos");
        jdbcTemplate.execute("DELETE FROM produtos");

        // Inserir produtos para testes BDD
        jdbcTemplate.execute(
            "INSERT INTO produtos (nome, descricao, preco, categoria) " +
            "VALUES ('X-Burger', 'Hambúrguer com queijo', 15.90, 'LANCHE')"
        );
        jdbcTemplate.execute(
            "INSERT INTO produtos (nome, descricao, preco, categoria) " +
            "VALUES ('X-Bacon', 'Hambúrguer com bacon', 18.90, 'LANCHE')"
        );
        jdbcTemplate.execute(
            "INSERT INTO produtos (nome, descricao, preco, categoria) " +
            "VALUES ('Batata Frita', 'Porção média', 10.00, 'ACOMPANHAMENTO')"
        );
        jdbcTemplate.execute(
            "INSERT INTO produtos (nome, descricao, preco, categoria) " +
            "VALUES ('Coca-Cola', 'Lata 350ml', 7.00, 'BEBIDA')"
        );
    }
}
