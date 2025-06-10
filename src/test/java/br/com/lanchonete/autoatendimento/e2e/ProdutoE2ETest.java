package br.com.lanchonete.autoatendimento.e2e;

import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.adaptadores.web.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoGateway;
import br.com.lanchonete.autoatendimento.entidades.produto.Categoria;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProdutoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoGateway produtoGateway;

    private Produto produtoPreCadastrado;

    @BeforeEach
    void configurar() {
        // Pré-cadastra um produto para os testes
        produtoPreCadastrado = Produto.criar("X-Bacon",
                "Hambúrguer com bacon crocante",
                new BigDecimal("29.90"),
                Categoria.LANCHE);

        produtoPreCadastrado = produtoGateway.salvar(produtoPreCadastrado);
    }

    @Test
    @DisplayName("Deve criar um produto com sucesso")
    void t1() throws Exception {

        ProdutoRequestDTO novoProduto = new ProdutoRequestDTO("Batata Frita",
                "Porção de batata frita crocante", new BigDecimal("15.90"), Categoria.ACOMPANHAMENTO);


        MvcResult resultado = mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProduto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Batata Frita"))
                .andExpect(jsonPath("$.preco").value(15.90))
                .andExpect(jsonPath("$.descricao").value("Porção de batata frita crocante"))
                .andExpect(jsonPath("$.categoria").value("ACOMPANHAMENTO"))
                .andReturn();


        String respostaJson = resultado.getResponse().getContentAsString();
        ProdutoResponseDTO produtoCriado = objectMapper.readValue(respostaJson, ProdutoResponseDTO.class);

        // Verifica se o produto foi realmente persistido no banco
        Optional<Produto> produtoPersistido = produtoGateway.buscarPorId(produtoCriado.id());
        assertTrue(produtoPersistido.isPresent(), "O produto deve existir no banco de dados");
        assertEquals("Batata Frita", produtoPersistido.get().getNome());
        assertEquals(new BigDecimal("15.90"), produtoPersistido.get().getPreco());
    }

    @Test
    @DisplayName("Deve editar um produto com sucesso")
    void t2() throws Exception {

        ProdutoRequestDTO produtoParaEditar = new ProdutoRequestDTO("X-Bacon Especial",
                "Hambúrguer com bacon crocante e molho especial", new BigDecimal("32.90"), Categoria.LANCHE);

        mockMvc.perform(put("/produtos/{id}", produtoPreCadastrado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoParaEditar)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(produtoPreCadastrado.getId()))
                .andExpect(jsonPath("$.nome").value("X-Bacon Especial"))
                .andExpect(jsonPath("$.descricao").value("Hambúrguer com bacon crocante e molho especial"))
                .andExpect(jsonPath("$.preco").value(32.90))
                .andReturn();

        // Verifica se o produto foi realmente atualizado no banco
        Optional<Produto> produtoAtualizado = produtoGateway.buscarPorId(produtoPreCadastrado.getId());
        assertTrue(produtoAtualizado.isPresent(), "O produto deve existir no banco de dados");
        assertEquals("X-Bacon Especial", produtoAtualizado.get().getNome());
        assertEquals(new BigDecimal("32.90"), produtoAtualizado.get().getPreco());
        assertEquals(Categoria.LANCHE, produtoAtualizado.get().getCategoria());
        assertEquals("Hambúrguer com bacon crocante e molho especial", produtoAtualizado.get().getDescricao());
    }

    @Test
    @DisplayName("Deve remover um produto com sucesso")
    void t3() throws Exception {
        // Executa a requisição DELETE para remover o produto
        mockMvc.perform(delete("/produtos/{id}", produtoPreCadastrado.getId()))
                .andExpect(status().isNoContent());

        // Verificar se o produto foi realmente removido do banco
        Optional<Produto> produtoRemovido = produtoGateway.buscarPorId(produtoPreCadastrado.getId());
        assertFalse(produtoRemovido.isPresent(), "O produto não deve existir mais no banco de dados");
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void t4() throws Exception {
        // Adiciona outro produto na mesma categoria do produto pré cadastrado
        Produto outroLanche = Produto.criar("X-Salada",
                "Hambúrguer com alface e tomate",
                new BigDecimal("25.90"),
                Categoria.LANCHE);
        produtoGateway.salvar(outroLanche);

        // Adiciona produto de outra categoria
        Produto novaBebiba = Produto.criar("Refrigerante Cola",
                "Refrigerante de cola 350ml",
                new BigDecimal("5.90"),
                Categoria.BEBIDA);
        produtoGateway.salvar(novaBebiba);

        // Busca produtos da categoria LANCHE
        mockMvc.perform(get("/produtos/categoria/{categoria}", "LANCHE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();

        // Busca produtos da categoria BEBIDA
        mockMvc.perform(get("/produtos/categoria/{categoria}", "BEBIDA"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Refrigerante Cola"))
                .andReturn();

        // Busca produtos de uma categoria sem produtos
        mockMvc.perform(get("/produtos/categoria/{categoria}", "SOBREMESA"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar criar produto com nome duplicado")
    void t5() throws Exception {
        // Tenta criar um produto com o mesmo nome de um produto existente
        ProdutoRequestDTO novoProdutoComNomeDuplicado = new ProdutoRequestDTO("X-Bacon",
                "Outro hambúrguer com bacon", new BigDecimal("27.90"), Categoria.LANCHE);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProdutoComNomeDuplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Já existe um produto com este nome"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar criar produto com dados inválidos")
    void t6() throws Exception {

        // Produto sem nome
        ProdutoRequestDTO novoProdutoSemNome = new ProdutoRequestDTO("", "Descrição teste", new BigDecimal("19.90"), Categoria.LANCHE);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProdutoSemNome)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nome do produto é obrigatório"));

        // Produto com preço negativo
        ProdutoRequestDTO novoProdutoPrecoNegativo = new ProdutoRequestDTO("Produto Teste",
                "Descrição teste", new BigDecimal("-5.90"), Categoria.LANCHE);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProdutoPrecoNegativo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Preço deve ser maior que zero"));
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar editar produto inexistente")
    void t7() throws Exception {

        ProdutoRequestDTO produtoParaEditarInexistente = new ProdutoRequestDTO("Produto Inexistente",
                "Descrição teste",new BigDecimal("19.90"), Categoria.LANCHE);


        mockMvc.perform(put("/produtos/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoParaEditarInexistente)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar remover produto inexistente")
    void t8() throws Exception {
        mockMvc.perform(delete("/produtos/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}