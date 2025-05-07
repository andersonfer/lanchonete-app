package br.com.lanchonete.autoatendimento.adaptadores.rest;

import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteRequestDTO;
import br.com.lanchonete.autoatendimento.aplicacao.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.cliente.CadastrarClienteCasoDeUso;
import br.com.lanchonete.autoatendimento.aplicacao.casosdeuso.interfaces.cliente.IdentificarClienteCasoDeUso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CadastrarClienteCasoDeUso cadastrarClienteCasoDeUso;
    private final IdentificarClienteCasoDeUso identificarClienteCasoDeUso;

    @PostMapping
    @Operation(
        summary = "Cadastrar cliente",
        description = "Cadastra um novo cliente no sistema",
        responses = {
                @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado")
        }
    )
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(@RequestBody ClienteRequestDTO novoCliente) {
        ClienteResponseDTO clienteCadastrado = cadastrarClienteCasoDeUso.executar(novoCliente);
        return new ResponseEntity<>(clienteCadastrado, HttpStatus.CREATED);
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(
            summary = "Buscar cliente por CPF",
            description = "Identifica um novo cliente no sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
            }
    )
    public ResponseEntity<ClienteResponseDTO> identificarPorCpf(@PathVariable String cpf) {
        return identificarClienteCasoDeUso.executar(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
