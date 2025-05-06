package br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.aplicacao.portas.saida.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.dominio.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RealizarPedido implements RealizarPedidoUC {

    private final PedidoRepositorio pedidoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    @Override
    @Transactional
    public PedidoResponseDTO executar(PedidoRequestDTO novoPedido) {
        
        validarPedido(novoPedido);
        
        Cliente cliente = buscarCliente(novoPedido.cpfCliente());

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .status(StatusPedido.RECEBIDO)
                .dataCriacao(LocalDateTime.now())
                .itens(new ArrayList<>())
                .build();
        
        adicionarItensAoPedido(pedido,novoPedido.itens());
        pedido.calcularValorTotal();

        Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

        return PedidoResponseDTO.converterParaDTO(pedidoSalvo);
    }

    private void adicionarItensAoPedido(Pedido pedido, List<ItemPedidoDTO> itens) {

        for (ItemPedidoDTO itemDTO : itens) {
            Produto produto = produtoRepositorio.buscarPorId(itemDTO.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + itemDTO.produtoId()));

            ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemDTO.quantidade())
                    .valorUnitario(produto.getPreco())
                    .build();

            item.calcularValorTotal();
            pedido.adicionarItem(item);
        }
    }

    private Cliente buscarCliente(String cpf) {
       if (StringUtils.isNotBlank(cpf)) {
           return clienteRepositorio.buscarPorCpf(cpf)
                   .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o CPF informado"));
       } else {
           return null;
       }
   }

   private void validarPedido(PedidoRequestDTO pedidoRequest) {
        if (pedidoRequest == null) {
            throw new ValidacaoException("Pedido não pode ser nulo");
        }

        if (pedidoRequest.itens() == null || pedidoRequest.itens().isEmpty()) {
            throw new ValidacaoException("Pedido deve conter pelo menos um item");
        }

        for (ItemPedidoDTO item : pedidoRequest.itens()) {
            if (item.produtoId() == null) {
                throw new ValidacaoException("ID do produto é obrigatório");
            }

            if (item.quantidade() <= 0) {
                throw new ValidacaoException("Quantidade deve ser maior que zero");
            }
        }
    }
}