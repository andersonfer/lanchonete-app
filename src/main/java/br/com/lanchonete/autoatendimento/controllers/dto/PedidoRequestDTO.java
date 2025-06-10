package br.com.lanchonete.autoatendimento.controllers.dto;

import java.util.List;

public record PedidoRequestDTO(
        String cpfCliente,       // Opcional - para clientes identificados
        List<ItemPedidoDTO> itens
) {}