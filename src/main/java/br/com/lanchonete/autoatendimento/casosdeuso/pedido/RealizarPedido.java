package br.com.lanchonete.autoatendimento.casosdeuso.pedido;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ItemPedidoDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.PedidoResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.RecursoNaoEncontradoException;
import br.com.lanchonete.autoatendimento.aplicacao.excecao.ValidacaoException;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.pedido.RealizarPedidoUC;
import br.com.lanchonete.autoatendimento.interfaces.ClienteRepositorio;
import br.com.lanchonete.autoatendimento.interfaces.PedidoRepositorio;
import br.com.lanchonete.autoatendimento.interfaces.ProdutoRepositorio;
import br.com.lanchonete.autoatendimento.entidades.cliente.Cliente;
import br.com.lanchonete.autoatendimento.entidades.pedido.ItemPedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.Pedido;
import br.com.lanchonete.autoatendimento.entidades.pedido.StatusPedido;
import br.com.lanchonete.autoatendimento.entidades.produto.Produto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RealizarPedido implements RealizarPedidoUC {

    private final PedidoRepositorio pedidoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    @Override
    @Transactional
    public PedidoResponseDTO executar(final PedidoRequestDTO novoPedido) {

        if (novoPedido == null) {
            throw new ValidacaoException("Pedido não pode ser nulo");
        }

        try {
            final Cliente cliente = buscarCliente(novoPedido.cpfCliente());

            final Pedido pedido = Pedido.criar(
                    cliente,
                    StatusPedido.RECEBIDO,
                    LocalDateTime.now()
            );

            adicionarItensAoPedido(pedido,novoPedido.itens());

            pedido.validar();

            final Pedido pedidoSalvo = pedidoRepositorio.salvar(pedido);

            return PedidoResponseDTO.converterParaDTO(pedidoSalvo);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException(e.getMessage());
        }

    }

    private void adicionarItensAoPedido(final Pedido pedido, final List<ItemPedidoDTO> itens) {

        for (final ItemPedidoDTO itemDTO : itens) {
            final Produto produto = produtoRepositorio.buscarPorId(itemDTO.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + itemDTO.produtoId()));

            final ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemDTO.quantidade())
                    .valorUnitario(produto.getPreco())
                    .build();

            item.calcularValorTotal();
            pedido.adicionarItem(item);
        }
    }

    private Cliente buscarCliente(final String cpf) {
       if (StringUtils.isNotBlank(cpf)) {
           return clienteRepositorio.buscarPorCpf(cpf)
                   .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o CPF informado"));
       } else {
           return null;
       }
   }

}