package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RepositorioConfig.class, JdbcConfig.class})
class RepositorioConfigTest {

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Autowired
    private ProdutoRepositorio produtoRepositorio;

    @Test
    @DisplayName( "Deve criar os beans de repositorio")
    void t1() {
        assertNotNull(clienteRepositorio);
        assertNotNull(produtoRepositorio);
    }
}