package br.com.lanchonete.clientes.adapters.web.controller;

import br.com.lanchonete.clientes.adapters.web.dto.ClienteRequest;
import br.com.lanchonete.clientes.adapters.web.dto.ClienteResponse;
import br.com.lanchonete.clientes.adapters.web.dto.IdentificarClienteRequest;
import br.com.lanchonete.clientes.adapters.web.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Endpoints para gestão de clientes")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;

    public ClienteController(final ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(
            summary = "Cadastrar novo cliente",
            description = "Cria um novo cliente no sistema com nome, email e CPF"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou CPF já cadastrado"
            )
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody final ClienteRequest request) {
        log.info("CI/CD Test - Build 2025-11-17-v1 - Cadastro de cliente solicitado para CPF: {}", request.cpf());
        final ClienteResponse response = clienteService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Identificar cliente por CPF",
            description = "Busca e retorna os dados de um cliente utilizando o CPF"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })
    @PostMapping("/identificar")
    public ResponseEntity<ClienteResponse> identificar(@RequestBody final IdentificarClienteRequest request) {
        log.info("CD Versionado - Identificação de cliente solicitada para CPF: {}", request.cpf());
        final ClienteResponse response = clienteService.identificar(request.cpf());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Buscar cliente por CPF",
            description = "Retorna os dados completos de um cliente a partir do CPF fornecido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<ClienteResponse> buscarPorCpf(@PathVariable final String cpf) {
        final ClienteResponse response = clienteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(response);
    }
}
