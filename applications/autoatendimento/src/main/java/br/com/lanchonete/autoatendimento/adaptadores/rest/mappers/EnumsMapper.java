package br.com.lanchonete.autoatendimento.adaptadores.rest.mappers;

import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.CategoriaDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPagamentoDTO;
import br.com.lanchonete.autoatendimento.adaptadores.rest.dto.StatusPedidoDTO;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;
import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.dominio.modelo.produto.Categoria;
import org.springframework.stereotype.Component;

@Component
public class EnumsMapper {

    public CategoriaDTO categoriaParaDTO(Categoria categoria) {
        return switch (categoria) {
            case LANCHE -> CategoriaDTO.LANCHE;
            case ACOMPANHAMENTO -> CategoriaDTO.ACOMPANHAMENTO;
            case BEBIDA -> CategoriaDTO.BEBIDA;
            case SOBREMESA -> CategoriaDTO.SOBREMESA;
        };
    }

    public Categoria categoriaParaDominio(CategoriaDTO categoriaDTO) {
        return switch (categoriaDTO) {
            case LANCHE -> Categoria.LANCHE;
            case ACOMPANHAMENTO -> Categoria.ACOMPANHAMENTO;
            case BEBIDA -> Categoria.BEBIDA;
            case SOBREMESA -> Categoria.SOBREMESA;
        };
    }

    public StatusPagamentoDTO statusPagamentoParaDTO(StatusPagamento statusPagamento) {
        return switch (statusPagamento) {
            case PENDENTE -> StatusPagamentoDTO.PENDENTE;
            case APROVADO -> StatusPagamentoDTO.APROVADO;
            case REJEITADO -> StatusPagamentoDTO.REJEITADO;
        };
    }

    public StatusPagamento statusPagamentoParaDominio(StatusPagamentoDTO statusPagamentoDTO) {
        return switch (statusPagamentoDTO) {
            case PENDENTE -> StatusPagamento.PENDENTE;
            case APROVADO -> StatusPagamento.APROVADO;
            case REJEITADO -> StatusPagamento.REJEITADO;
        };
    }

    public StatusPedidoDTO statusPedidoParaDTO(StatusPedido statusPedido) {
        return switch (statusPedido) {
            case RECEBIDO -> StatusPedidoDTO.RECEBIDO;
            case EM_PREPARACAO -> StatusPedidoDTO.EM_PREPARACAO;
            case PRONTO -> StatusPedidoDTO.PRONTO;
            case FINALIZADO -> StatusPedidoDTO.FINALIZADO;
        };
    }

    public StatusPedido statusPedidoParaDominio(StatusPedidoDTO statusPedidoDTO) {
        return switch (statusPedidoDTO) {
            case RECEBIDO -> StatusPedido.RECEBIDO;
            case EM_PREPARACAO -> StatusPedido.EM_PREPARACAO;
            case PRONTO -> StatusPedido.PRONTO;
            case FINALIZADO -> StatusPedido.FINALIZADO;
        };
    }
}