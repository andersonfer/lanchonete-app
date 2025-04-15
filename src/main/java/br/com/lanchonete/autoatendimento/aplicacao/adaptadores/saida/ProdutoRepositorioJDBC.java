package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.saida;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.RegistroNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Categoria;
import br.com.lanchonete.autoatendimento.dominio.Produto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProdutoRepositorioJDBC implements ProdutoRepositorio {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert inserter;

    public ProdutoRepositorioJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.inserter = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("produto")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Produto> produtoRowMapper = (rs, rowNum) ->
            Produto.builder()
                    .id(rs.getLong("id"))
                    .nome(rs.getString("nome"))
                    .descricao(rs.getString("descricao"))
                    .preco(rs.getBigDecimal("preco"))
                    .categoria(Categoria.valueOf(rs.getString("categoria")))
                    .build();

    @Override
    public Produto salvar(Produto produto) {
        Map<String, Object> params = new HashMap<>();
        params.put("nome", produto.getNome());
        params.put("descricao", produto.getDescricao());
        params.put("preco", produto.getPreco());
        params.put("categoria", produto.getCategoria().name());

        Number novoId = inserter.executeAndReturnKey(params);
        produto.setId(novoId.longValue());
        return produto;
    }

    @Override
    public Produto atualizar(Produto produto) {
        String sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, categoria = ? WHERE id = ?";

        int linhasAfetadas = jdbcTemplate.update(sql,
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria().name(),
                produto.getId());

        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Produto", produto.getId());
        }

        return produto;
    }

    @Override
    public void remover(Long id) {
        String sql = "DELETE FROM produto WHERE id = ?";
        int linhasAfetadas = jdbcTemplate.update(sql, id);

        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Produto", id);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        try {
            String sql = "SELECT * FROM produto WHERE id = ?";
            Produto produto = jdbcTemplate.queryForObject(sql, produtoRowMapper, id);
            return Optional.ofNullable(produto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Produto> buscarPorCategoria(Categoria categoria) {
        String sql = "SELECT * FROM produto WHERE categoria = ?";
        return jdbcTemplate.query(sql, produtoRowMapper, categoria.name());
    }

    @Override
    public List<Produto> listarTodos() {
        String sql = "SELECT * FROM produto";
        return jdbcTemplate.query(sql, produtoRowMapper);
    }

    @Override
    public boolean existePorId(Long id) {
        String sql = "SELECT COUNT(*) FROM produto WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existePorNome(String nome) {
        String sql = "SELECT COUNT(*) FROM produto WHERE nome = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nome);
        return count != null && count > 0;
    }
}