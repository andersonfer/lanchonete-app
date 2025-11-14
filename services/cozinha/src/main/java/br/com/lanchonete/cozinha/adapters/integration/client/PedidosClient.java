package br.com.lanchonete.cozinha.adapters.integration.client;

import br.com.lanchonete.cozinha.adapters.integration.dto.PedidoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pedidos", url = "${pedidos.service.url}")
public interface PedidosClient {

    @GetMapping("/pedidos/{id}")
    PedidoDto buscarPedido(@PathVariable("id") Long id);
}
