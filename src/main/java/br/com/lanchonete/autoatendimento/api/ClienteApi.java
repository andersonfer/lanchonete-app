package br.com.lanchonete.autoatendimento.api;

import br.com.lanchonete.autoatendimento.controllers.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.controllers.dto.ClienteResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
public interface ClienteApi {

    @PostMapping
    @Operation(
        summary = "Cadastrar cliente",
        description = "Cadastra um novo cliente no sistema",
        responses = {
                @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado")
        }
    )
    ResponseEntity<ClienteResponseDTO> cadastrarCliente(@RequestBody ClienteRequestDTO novoCliente);

    @GetMapping("/cpf/{cpf}")
    @Operation(
            summary = "Buscar cliente por CPF",
            description = "Identifica um cliente no sistema pelo CPF",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
            }
    )
    ResponseEntity<ClienteResponseDTO> identificarPorCpf(@PathVariable String cpf);
}