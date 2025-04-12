package br.com.lanchonete.autoatendimento.infra;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.BuscarProdutosPorCategoriaService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.CriarProdutoService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.EditarProdutoService;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.RemoverProdutoService;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProdutoServiceConfig.class})
public class ProdutoServiceConfigTest {

    @MockitoBean
    private ProdutoRepositorio produtoRepositorio;

    @Autowired
    private BuscarProdutosPorCategoriaService buscarProdutosPorCategoriaService;

    @Autowired
    private CriarProdutoService criarProdutoService;

    @Autowired
    private EditarProdutoService editarProdutoService;

    @Autowired
    private RemoverProdutoService removerProdutoService;

    @Test
    @DisplayName("Deve criar os beans dos UCs de produto")
    void t1() {
        assertNotNull(criarProdutoService);
        assertNotNull(editarProdutoService);
        assertNotNull(removerProdutoService);
        assertNotNull(buscarProdutosPorCategoriaService);
    }
}
