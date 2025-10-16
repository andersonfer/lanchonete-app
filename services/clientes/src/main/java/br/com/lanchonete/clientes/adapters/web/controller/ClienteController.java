package br.com.lanchonete.clientes.adapters.web.controller;

import br.com.lanchonete.clientes.adapters.web.dto.ClienteRequest;
import br.com.lanchonete.clientes.adapters.web.dto.ClienteResponse;
import br.com.lanchonete.clientes.adapters.web.dto.IdentificarClienteRequest;
import br.com.lanchonete.clientes.adapters.web.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(final ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody final ClienteRequest request) {
        final ClienteResponse response = clienteService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/identificar")
    public ResponseEntity<ClienteResponse> identificar(@RequestBody final IdentificarClienteRequest request) {
        final ClienteResponse response = clienteService.identificar(request.cpf());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<ClienteResponse> buscarPorCpf(@PathVariable final String cpf) {
        final ClienteResponse response = clienteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(response);
    }
}
