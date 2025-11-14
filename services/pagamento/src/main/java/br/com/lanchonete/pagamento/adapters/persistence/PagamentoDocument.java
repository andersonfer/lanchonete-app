package br.com.lanchonete.pagamento.adapters.persistence;

import br.com.lanchonete.pagamento.domain.model.StatusPagamento;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "pagamentos")
public class PagamentoDocument {

    @Id
    private String id;

    @Field("pedido_id")
    private Long pedidoId;

    @Field("valor")
    private BigDecimal valor;

    @Field("status")
    private String status;

    @Field("data_criacao")
    private LocalDateTime dataCriacao;

    public PagamentoDocument() {
    }

    public PagamentoDocument(Long pedidoId, BigDecimal valor, StatusPagamento status, LocalDateTime dataCriacao) {
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = status.name();
        this.dataCriacao = dataCriacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
