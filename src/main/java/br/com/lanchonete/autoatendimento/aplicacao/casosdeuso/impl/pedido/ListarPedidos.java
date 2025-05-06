package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.impl.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.pedido.ListarPedidosUC;
import br.com.lanchonete.autoatendimento.aplicacao.repositorios.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.dominio.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarPedidos implements ListarPedidosUC {

    private final PedidoRepositorio pedidoRepositorio;

    @Override
    public List<PedidoResponseDTO> executar() {
        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        return pedidos.stream()
                .map(PedidoResponseDTO::converterParaDTO)
                .collect(Collectors.toList());
    }
}