package br.com.lanchonete.pedidos.adapters.persistence;

import br.com.lanchonete.pedidos.application.gateways.ProdutoGateway;
import br.com.lanchonete.pedidos.domain.model.Categoria;
import br.com.lanchonete.pedidos.domain.model.Produto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ProdutoRepositoryJdbc implements ProdutoGateway {

    private final JdbcTemplate jdbcTemplate;

    public ProdutoRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        List<Produto> produtos = jdbcTemplate.query(sql, new ProdutoRowMapper(), id);
        return produtos.isEmpty() ? Optional.empty() : Optional.of(produtos.get(0));
    }

    @Override
    public List<Produto> listarTodos() {
        String sql = "SELECT * FROM produtos ORDER BY categoria, nome";
        return jdbcTemplate.query(sql, new ProdutoRowMapper());
    }

    @Override
    public List<Produto> listarPorCategoria(Categoria categoria) {
        String sql = "SELECT * FROM produtos WHERE categoria = ? ORDER BY nome";
        return jdbcTemplate.query(sql, new ProdutoRowMapper(), categoria.name());
    }

    private static class ProdutoRowMapper implements RowMapper<Produto> {
        @Override
        public Produto mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            String nome = rs.getString("nome");
            String descricao = rs.getString("descricao");
            BigDecimal preco = rs.getBigDecimal("preco");
            Categoria categoria = Categoria.valueOf(rs.getString("categoria"));

            return Produto.reconstituir(id, nome, descricao, preco, categoria);
        }
    }
}
