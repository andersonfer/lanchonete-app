package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ProdutoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
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
    private ProdutoRepositorio produtoRepositorio;

    private Produto produtoPreCadastrado;

    @BeforeEach
    void configurar() {
        // Pré-cadastra um produto para os testes
        produtoPreCadastrado = Produto.builder()
                .nome("X-Bacon")
                .descricao("Hambúrguer com bacon crocante")
                .preco(new BigDecimal("29.90"))
                .categoria(Categoria.LANCHE)
                .build();

        produtoPreCadastrado = produtoRepositorio.salvar(produtoPreCadastrado);
    }

    @Test
    @DisplayName("Deve criar um produto com sucesso")
    void t1() throws Exception {

        ProdutoRequestDTO requisicao = ProdutoRequestDTO.builder()
                .nome("Batata Frita")
                .descricao("Porção de batata frita crocante")
                .preco(new BigDecimal("15.90"))
                .categoria(Categoria.ACOMPANHAMENTO)
                .build();


        MvcResult resultado = mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
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
        Optional<Produto> produtoPersistido = produtoRepositorio.buscarPorId(produtoCriado.getId());
        assertTrue(produtoPersistido.isPresent(), "O produto deve existir no banco de dados");
        assertEquals("Batata Frita", produtoPersistido.get().getNome());
        assertEquals(new BigDecimal("15.90"), produtoPersistido.get().getPreco());
    }

    @Test
    @DisplayName("Deve editar um produto com sucesso")
    void t2() throws Exception {

        ProdutoRequestDTO requisicao = ProdutoRequestDTO.builder()
                .nome("X-Bacon Especial")
                .descricao("Hambúrguer com bacon crocante e molho especial")
                .preco(new BigDecimal("32.90"))
                .categoria(Categoria.LANCHE)
                .build();


        mockMvc.perform(put("/produtos/{id}", produtoPreCadastrado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(produtoPreCadastrado.getId()))
                .andExpect(jsonPath("$.nome").value("X-Bacon Especial"))
                .andExpect(jsonPath("$.descricao").value("Hambúrguer com bacon crocante e molho especial"))
                .andExpect(jsonPath("$.preco").value(32.90))
                .andReturn();

        // Verifica se o produto foi realmente atualizado no banco
        Optional<Produto> produtoAtualizado = produtoRepositorio.buscarPorId(produtoPreCadastrado.getId());
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
        Optional<Produto> produtoRemovido = produtoRepositorio.buscarPorId(produtoPreCadastrado.getId());
        assertFalse(produtoRemovido.isPresent(), "O produto não deve existir mais no banco de dados");
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void t4() throws Exception {
        // Adiciona outro produto na mesma categoria do produto pré cadastrado
        Produto outroLanche = Produto.builder()
                .nome("X-Salada")
                .descricao("Hambúrguer com alface e tomate")
                .preco(new BigDecimal("25.90"))
                .categoria(Categoria.LANCHE)
                .build();
        produtoRepositorio.salvar(outroLanche);

        // Adiciona produto de outra categoria
        Produto novaBebiba = Produto.builder()
                .nome("Refrigerante Cola")
                .descricao("Refrigerante de cola 350ml")
                .preco(new BigDecimal("5.90"))
                .categoria(Categoria.BEBIDA)
                .build();
        produtoRepositorio.salvar(novaBebiba);

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
        ProdutoRequestDTO requisicao = ProdutoRequestDTO.builder()
                .nome("X-Bacon") // Nome já existente
                .descricao("Outro hambúrguer com bacon")
                .preco(new BigDecimal("27.90"))
                .categoria(Categoria.LANCHE)
                .build();

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Já existe um produto com este nome"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar criar produto com dados inválidos")
    void t6() throws Exception {
        // Produto sem nome
        ProdutoRequestDTO requisicaoSemNome = ProdutoRequestDTO.builder()
                .nome("")
                .descricao("Descrição teste")
                .preco(new BigDecimal("19.90"))
                .categoria(Categoria.LANCHE)
                .build();

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoSemNome)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nome do produto é obrigatório"));

        // Produto com preço negativo
        ProdutoRequestDTO requisicaoPrecoNegativo = ProdutoRequestDTO.builder()
                .nome("Produto Teste")
                .descricao("Descrição teste")
                .preco(new BigDecimal("-5.90"))
                .categoria(Categoria.LANCHE)
                .build();

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoPrecoNegativo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Preço deve ser maior que zero"));
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar editar produto inexistente")
    void t7() throws Exception {
        ProdutoRequestDTO requisicao = ProdutoRequestDTO.builder()
                .nome("Produto Inexistente")
                .descricao("Descrição teste")
                .preco(new BigDecimal("19.90"))
                .categoria(Categoria.LANCHE)
                .build();

        mockMvc.perform(put("/produtos/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao tentar remover produto inexistente")
    void t8() throws Exception {
        mockMvc.perform(delete("/produtos/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}