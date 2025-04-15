package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.ListarPedidosUC;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarPedidosService implements ListarPedidosUC {

    private final PedidoRepositorio pedidoRepositorio;

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        List<Pedido> pedidos = pedidoRepositorio.listarTodos();

        return pedidos.stream()
                .map(PedidoResponseDTO::converterParaDTO)
                .collect(Collectors.toList());
    }
}