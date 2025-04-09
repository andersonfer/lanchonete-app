package br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada;

import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.CadastrarClienteDTO;
import br.com.lanchonete.autoatendimento.aplicacao.adaptadores.entrada.dto.ClienteResponseDTO;
import br.com.lanchonete.autoatendimento.aplicacao.portas.entrada.CadastrarClienteUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gerenciamento de clientes")
public class CadastrarClienteController {

    private final CadastrarClienteUC cadastrarClienteUC;

    public CadastrarClienteController(CadastrarClienteUC cadastrarClienteUC) {
        this.cadastrarClienteUC = cadastrarClienteUC;
    }

    @PostMapping
    @Operation(summary = "Cadastrar cliente", description = "Cadastra um novo cliente")
    @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(@RequestBody CadastrarClienteDTO novoCliente) {
        ClienteResponseDTO clienteCadastrado = cadastrarClienteUC.cadastrar(novoCliente);
        return new ResponseEntity<>(clienteCadastrado, HttpStatus.CREATED);
    }
}
