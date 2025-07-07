package br.com.lanchonete.autoatendimento.aplicacao.portas.entrada;

import br.com.lanchonete.autoatendimento.dominio.modelo.pedido.StatusPagamento;

public interface ConsultarStatusPagamentoUC {
    
    StatusPagamento executar(Long pedidoId);
}