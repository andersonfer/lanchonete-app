package br.com.lanchonete.cozinha.bdd.hooks;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseHooks {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before("@comPedidos")
    public void setupPedidosNaFila() {
        // Limpar dados
        jdbcTemplate.execute("DELETE FROM pedido_cozinha");

        // Inserir pedidos na fila para testes
        jdbcTemplate.execute(
            "INSERT INTO pedido_cozinha (pedido_id, status, data_inicio) " +
            "VALUES (101, 'AGUARDANDO', CURRENT_TIMESTAMP)"
        );
        jdbcTemplate.execute(
            "INSERT INTO pedido_cozinha (pedido_id, status, data_inicio) " +
            "VALUES (102, 'EM_PREPARO', CURRENT_TIMESTAMP)"
        );
    }

    @Before("@semPedidos")
    public void limparFila() {
        // Limpar todos os pedidos
        jdbcTemplate.execute("DELETE FROM pedido_cozinha");
    }
}
