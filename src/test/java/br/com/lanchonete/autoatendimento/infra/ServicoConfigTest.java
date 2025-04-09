package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.CadastrarClienteService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.IdentificarClienteService;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServicoConfig.class})
class ServicoConfigTest {

    @MockitoBean
    private ClienteRepositorio clienteRepositorio;

    @Autowired
    private CadastrarClienteService cadastrarClienteService;

    @Autowired
    private IdentificarClienteService identificarClienteService;

    @Test
    @DisplayName("Deve criar os beans Service")
    void t1() {
        assertNotNull(cadastrarClienteService);
        assertNotNull(identificarClienteService);
    }
}