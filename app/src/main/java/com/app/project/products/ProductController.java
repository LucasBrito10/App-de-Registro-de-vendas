package com.app.project.products;

import com.app.project.shared.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.math.BigDecimal;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductController {

    private final ProductService productService;
    private final Gson gson;

    private static final String BASE_URI = "/api/v1/products";
    private static final Pattern PRODUCT_BY_ID_PATTERN = Pattern.compile("^" + BASE_URI + "/(\\d+)$");

    public ProductController(ProductService productService) {
        this.productService = productService;
        this.gson = GsonFactory.create();
    }

    

    public Response handleGetRequest(IHTTPSession session) {
        String uri = session.getUri();

        try {
            Matcher matcher = PRODUCT_BY_ID_PATTERN.matcher(uri);
            if (matcher.matches()) {
                return fetchProductById(matcher.group(1));
            }

            if (BASE_URI.equals(uri)) {
                return fetchProducts(session.getParameters());
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint GET não encontrado.");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "Parâmetro numérico inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar requisição.");
        }
    }

    

    public Response handlePostRequest(IHTTPSession session) {
        String uri = session.getUri();

        try {
            if (BASE_URI.equals(uri)) {
                return createProduct(session);
            }

            Matcher matcher = PRODUCT_BY_ID_PATTERN.matcher(uri);
            if (matcher.matches()) {
                return updateProduct(session, matcher.group(1));
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "ID de produto inválido.");
        } catch (IllegalArgumentException e) {
            return errorResponse(Status.BAD_REQUEST, e.getMessage());
        } catch (JsonSyntaxException e) {
            return errorResponse(Status.BAD_REQUEST, "JSON mal formatado.");
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return errorResponse(Status.INTERNAL_ERROR, "Erro de I/O ao processar a requisição.");
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar a requisição.");
        }
    }

    

    public Response handleDeleteRequest(IHTTPSession session) {
        try {
            Matcher matcher = PRODUCT_BY_ID_PATTERN.matcher(session.getUri());

            if (!matcher.matches()) {
                return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");
            }

            long id = Long.parseLong(matcher.group(1));

            boolean removed = productService.deleteProduct(id);
            if (!removed) {
                return errorResponse(Status.NOT_FOUND, "Produto não encontrado.");
            }

            return NanoHTTPD.newFixedLengthResponse(Status.NO_CONTENT, "application/json; charset=UTF-8", "");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "ID de produto inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar a requisição.");
        }
    }

    

    private Response fetchProductById(String idParam) {
        long productId = Long.parseLong(idParam);
        var product = productService.getProductById(productId);

        if (product == null) {
            return errorResponse(Status.NOT_FOUND, "Produto não encontrado.");
        }
        return jsonResponse(Status.OK, product);
    }

    private Response fetchProducts(Map<String, List<String>> params) {
        
        String productName = null;
        if (params.containsKey("name") && !params.get("name").isEmpty()) {
            productName = params.get("name").get(0);
        }

        if (params.containsKey("page") || params.containsKey("size")) {
            int page = Integer.parseInt(params.getOrDefault("page", List.of("1")).get(0));
            int size = Integer.parseInt(params.getOrDefault("size", List.of("10")).get(0));
            
            if (page < 1 || size < 1) {
                return errorResponse(Status.BAD_REQUEST, "Os parâmetros page e size devem ser maiores que zero.");
            }
            
            
            return jsonResponse(Status.OK, productService.getProductsPaginated(productName, page, size));
        }

        
        if (productName != null) {
            return jsonResponse(Status.OK, productService.getProductsByName(productName));
        }

        
        return jsonResponse(Status.OK, productService.getAllProducts());
}

    

    private Response createProduct(IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        
        ProductDto productRequest = parseRequestBody(session);

        if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
            return errorResponse(Status.BAD_REQUEST, "O nome é obrigatório.");
        }

        if (productRequest.getPrice() != null && productRequest.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return errorResponse(Status.BAD_REQUEST, "O preço não pode ser negativo.");
        }

        ProductDto createdProduct = productService.createProduct(productRequest);
        if (createdProduct == null) {
            return errorResponse(Status.INTERNAL_ERROR, "Não foi possível criar o produto.");
        }

        return jsonResponse(Status.CREATED, createdProduct);
    }

    private Response updateProduct(IHTTPSession session, String idParam) throws IOException, NanoHTTPD.ResponseException {
        
        long productId = Long.parseLong(idParam);
        ProductDto productRequest = parseRequestBody(session);

        if (productRequest.getName() == null && productRequest.getPrice() == null) {
            return errorResponse(Status.BAD_REQUEST, "Informe ao menos um campo para atualizar.");
        }

        if (productRequest.getName() != null && productRequest.getName().trim().isEmpty()) {
            return errorResponse(Status.BAD_REQUEST, "O nome não pode ser vazio.");
        }

        if (productRequest.getPrice() != null && productRequest.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return errorResponse(Status.BAD_REQUEST, "O preço não pode ser negativo.");
        }

        var updatedProduct = productService.updateProduct(productId, productRequest);
        if (updatedProduct == null) {
            return errorResponse(Status.NOT_FOUND, "Produto não encontrado.");
        }

        return jsonResponse(Status.OK, updatedProduct);
    }

    private ProductDto parseRequestBody(IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String postBody = files.get("postData");

        if (postBody == null || postBody.trim().isEmpty()) {
            throw new IllegalArgumentException("O corpo da requisição não pode estar vazio.");
        }

        ProductDto productRequest = gson.fromJson(postBody, ProductDto.class);
        if (productRequest == null) {
            throw new IllegalArgumentException("Corpo da requisição inválido ou vazio.");
        }

        return productRequest;
    }


    private Response jsonResponse(Status status, Object body) {
        return NanoHTTPD.newFixedLengthResponse(status, "application/json; charset=UTF-8", gson.toJson(body));
    }

    private Response errorResponse(Status status, String message) {
        String safeMessage = message != null ? message : "Erro ao processar a requisição.";
        return jsonResponse(status, Map.of("error", safeMessage));
    }
}