package br.com.lanchonete.autoatendimento.adaptadores.rest.dto;

import java.util.List;

public record PedidoRequestDTO(
        String cpfCliente,       // Opcional - para clientes identificados
        List<ItemPedidoRequestDTO> itens
) {}