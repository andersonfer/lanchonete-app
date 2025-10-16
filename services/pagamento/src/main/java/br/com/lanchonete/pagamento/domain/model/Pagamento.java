package br.com.lanchonete.pagamento.domain.model;

import br.com.lanchonete.pagamento.domain.exceptions.ValidacaoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Pagamento {

    private String id;
    private final Long pedidoId;
    private final Valor valor;
    private final StatusPagamento status;
    private final LocalDateTime dataCriacao;

    public Pagamento(Long pedidoId, Valor valor, StatusPagamento status) {
        validar(pedidoId, valor, status);
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = status;
        this.dataCriacao = LocalDateTime.now();
    }

    public Pagamento(String id, Long pedidoId, Valor valor, StatusPagamento status, LocalDateTime dataCriacao) {
        validar(pedidoId, valor, status);
        this.id = id;
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    private void validar(Long pedidoId, Valor valor, StatusPagamento status) {
        if (pedidoId == null) {
            throw new ValidacaoException("PedidoId não pode ser nulo");
        }

        if (pedidoId <= 0) {
            throw new ValidacaoException("PedidoId deve ser maior que zero");
        }

        if (valor == null) {
            throw new ValidacaoException("Valor não pode ser nulo");
        }

        if (status == null) {
            throw new ValidacaoException("Status não pode ser nulo");
        }
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

    public BigDecimal getValor() {
        return valor.getValor();
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return Objects.equals(id, pagamento.id) &&
               Objects.equals(pedidoId, pagamento.pedidoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pedidoId);
    }

    @Override
    public String toString() {
        return "Pagamento{" +
                "id='" + id + '\'' +
                ", pedidoId=" + pedidoId +
                ", valor=" + valor +
                ", status=" + status +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
