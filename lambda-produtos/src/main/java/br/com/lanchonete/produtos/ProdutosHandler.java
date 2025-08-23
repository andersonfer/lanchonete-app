package br.com.lanchonete.produtos;

import br.com.lanchonete.produtos.adaptadores.dtos.ProdutoRequestDTO;
import br.com.lanchonete.produtos.adaptadores.dtos.ProdutoResponseDTO;
import br.com.lanchonete.produtos.adaptadores.mappers.EnumsMapper;
import br.com.lanchonete.produtos.adaptadores.mappers.ProdutoMapper;
import br.com.lanchonete.produtos.adaptadores.mock.ProdutoMockGateway;
import br.com.lanchonete.produtos.adaptadores.servicos.ProdutoService;
import br.com.lanchonete.produtos.aplicacao.casosdeuso.*;
import br.com.lanchonete.produtos.aplicacao.gateways.ProdutoGateway;
import br.com.lanchonete.produtos.dominio.enums.CategoriaProduto;
import br.com.lanchonete.produtos.dominio.excecoes.RecursoNaoEncontradoException;
import br.com.lanchonete.produtos.dominio.excecoes.ValidacaoException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProdutosHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProdutoService produtoService;

    public ProdutosHandler() {
        // Inicialização das dependências (simulando injeção de dependência)
        ProdutoGateway produtoGateway = new ProdutoMockGateway();
        EnumsMapper enumsMapper = new EnumsMapper();
        ProdutoMapper produtoMapper = new ProdutoMapper(enumsMapper);
        
        // Casos de uso
        BuscarProdutosPorCategoria buscarProdutosPorCategoria = new BuscarProdutosPorCategoria(produtoGateway);
        CriarProduto criarProduto = new CriarProduto(produtoGateway);
        EditarProduto editarProduto = new EditarProduto(produtoGateway);
        RemoverProduto removerProduto = new RemoverProduto(produtoGateway);
        
        // Service
        this.produtoService = new ProdutoService(buscarProdutosPorCategoria, criarProduto, 
                                                editarProduto, removerProduto, produtoMapper);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String httpMethod = input.getHttpMethod();
            String path = input.getPath();
            Map<String, String> pathParameters = input.getPathParameters();

            // GET /produtos/categoria/{categoria}
            if ("GET".equals(httpMethod) && path.matches(".*/produtos/categoria/.*")) {
                return buscarPorCategoria(pathParameters.get("categoria"));
            }
            
            // POST /produtos
            if ("POST".equals(httpMethod) && path.endsWith("/produtos")) {
                return criarNovoProduto(input.getBody());
            }
            
            // PUT /produtos/{id}
            if ("PUT".equals(httpMethod) && path.matches(".*/produtos/\\d+")) {
                return editarProdutoExistente(pathParameters.get("id"), input.getBody());
            }
            
            // DELETE /produtos/{id}
            if ("DELETE".equals(httpMethod) && path.matches(".*/produtos/\\d+")) {
                return removerProdutoExistente(pathParameters.get("id"));
            }

            return criarResposta(404, Map.of("erro", "Endpoint não encontrado"));

        } catch (ValidacaoException e) {
            return criarResposta(400, Map.of("erro", e.getMessage()));
        } catch (RecursoNaoEncontradoException e) {
            return criarResposta(404, Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            context.getLogger().log("Erro interno: " + e.getMessage());
            return criarResposta(500, Map.of("erro", "Erro interno do servidor"));
        }
    }

    private APIGatewayProxyResponseEvent buscarPorCategoria(String categoriaStr) {
        try {
            CategoriaProduto categoria = CategoriaProduto.valueOf(categoriaStr.toUpperCase());
            List<ProdutoResponseDTO> produtos = produtoService.buscarPorCategoria(categoria);
            return criarResposta(200, produtos);
        } catch (IllegalArgumentException e) {
            return criarResposta(400, Map.of("erro", "Categoria inválida: " + categoriaStr));
        }
    }

    private APIGatewayProxyResponseEvent criarNovoProduto(String body) {
        try {
            ProdutoRequestDTO request = objectMapper.readValue(body, ProdutoRequestDTO.class);
            ProdutoResponseDTO produto = produtoService.criar(request);
            return criarResposta(201, produto);
        } catch (JsonProcessingException e) {
            return criarResposta(400, Map.of("erro", "JSON inválido"));
        }
    }

    private APIGatewayProxyResponseEvent editarProdutoExistente(String idStr, String body) {
        try {
            Long id = Long.parseLong(idStr);
            ProdutoRequestDTO request = objectMapper.readValue(body, ProdutoRequestDTO.class);
            ProdutoResponseDTO produto = produtoService.editar(id, request);
            return criarResposta(200, produto);
        } catch (NumberFormatException e) {
            return criarResposta(400, Map.of("erro", "ID deve ser um número"));
        } catch (JsonProcessingException e) {
            return criarResposta(400, Map.of("erro", "JSON inválido"));
        }
    }

    private APIGatewayProxyResponseEvent removerProdutoExistente(String idStr) {
        try {
            Long id = Long.parseLong(idStr);
            produtoService.remover(id);
            return criarResposta(204, null);
        } catch (NumberFormatException e) {
            return criarResposta(400, Map.of("erro", "ID deve ser um número"));
        }
    }

    private APIGatewayProxyResponseEvent criarResposta(int statusCode, Object body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeaders(headers);

        if (body != null) {
            try {
                response.setBody(objectMapper.writeValueAsString(body));
            } catch (JsonProcessingException e) {
                response.setStatusCode(500);
                response.setBody("{\"erro\":\"Erro ao serializar resposta\"}");
            }
        }

        return response;
    }
}