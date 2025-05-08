package br.com.lanchonete.autoatendimento.adaptadores.persistencia;

import br.com.lanchonete.autoatendimento.aplicacao.excecao.RegistroNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.modelo.Categoria;
import br.com.lanchonete.autoatendimento.dominio.modelo.Produto;
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
    private final RowMapper<Produto> produtoRowMapper = (rs, rowNum) ->

            Produto.criarSemValidacao(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getBigDecimal("preco"),
                    Categoria.valueOf(rs.getString("categoria")));

    public ProdutoRepositorioJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.inserter = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("produto")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Produto salvar(final Produto produto) {
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
    public Produto atualizar(final Produto produto) {
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
    public void remover(final Long id) {
        String sql = "DELETE FROM produto WHERE id = ?";
        int linhasAfetadas = jdbcTemplate.update(sql, id);

        if (linhasAfetadas == 0) {
            throw new RegistroNaoEncontradoException("Produto", id);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(final Long id) {
        try {
            String sql = "SELECT * FROM produto WHERE id = ?";
            Produto produto = jdbcTemplate.queryForObject(sql, produtoRowMapper, id);
            return Optional.ofNullable(produto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Produto> buscarPorCategoria(final Categoria categoria) {
        String sql = "SELECT * FROM produto WHERE categoria = ?";
        return jdbcTemplate.query(sql, produtoRowMapper, categoria.name());
    }

    @Override
    public List<Produto> listarTodos() {
        String sql = "SELECT * FROM produto";
        return jdbcTemplate.query(sql, produtoRowMapper);
    }

    @Override
    public boolean existePorId(final Long id) {
        String sql = "SELECT COUNT(*) FROM produto WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existePorNome(final String nome) {
        String sql = "SELECT COUNT(*) FROM produto WHERE nome = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nome);
        return count != null && count > 0;
    }
}