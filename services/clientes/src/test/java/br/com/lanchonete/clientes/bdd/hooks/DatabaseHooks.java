package br.com.lanchonete.clientes.bdd.hooks;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseHooks {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void limparBaseDeDados() {
        jdbcTemplate.execute("DELETE FROM cliente");
    }
}
