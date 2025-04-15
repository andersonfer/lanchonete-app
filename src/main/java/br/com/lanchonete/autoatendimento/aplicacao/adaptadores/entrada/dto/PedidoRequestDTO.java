package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto;

import java.util.List;

public record PedidoRequestDTO(
        String cpfCliente,       // Opcional - para clientes identificados
        List<ItemPedidoDTO> itens
) {}