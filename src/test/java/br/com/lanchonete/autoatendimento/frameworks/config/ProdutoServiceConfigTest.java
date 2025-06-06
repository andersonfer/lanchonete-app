package br.com.lanchonete.autoatendimento.frameworks.config;

import br.com.lanchonete.autoatendimento.casosdeuso.produto.BuscarProdutosPorCategoria;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.CriarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.EditarProduto;
import br.com.lanchonete.autoatendimento.casosdeuso.produto.RemoverProduto;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoRepositorio;
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
class ProdutoServiceConfigTest {

    @MockitoBean
    private ProdutoRepositorio produtoRepositorio;

    @Autowired
    private BuscarProdutosPorCategoria buscarProdutosPorCategoria;

    @Autowired
    private CriarProduto criarProduto;

    @Autowired
    private EditarProduto editarProduto;

    @Autowired
    private RemoverProduto removerProduto;

    @Test
    @DisplayName("Deve criar os beans dos UCs de produto")
    void t1() {
        assertNotNull(criarProduto);
        assertNotNull(editarProduto);
        assertNotNull(removerProduto);
        assertNotNull(buscarProdutosPorCategoria);
    }
}
