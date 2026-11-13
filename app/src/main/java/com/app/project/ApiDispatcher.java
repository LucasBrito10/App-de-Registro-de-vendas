package com.app.project;

import com.app.project.client.ClientController;
import com.app.project.products.ProductController;
import com.app.project.sale.SaleController;
import com.app.project.shared.NotFound;

import java.util.function.Function;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

// Encaminha cada requisição da API para o controller do recurso e, dentro dele,
// para o handler do método HTTP (GET, POST ou DELETE).
// A segurança já foi verificada pelo NanoServer antes de chegar aqui.
public class ApiDispatcher {

    private static final String CLIENTS_URI = "/api/v1/clients";
    private static final String PRODUCTS_URI = "/api/v1/products";
    private static final String SALES_URI = "/api/v1/sales";

    // Métodos aceitos pela API. Atualizações usam POST em /{id}, como nos controllers.
    private static final String ALLOWED_METHODS = "GET, POST, DELETE";

    private final ClientController clientController;
    private final ProductController productController;
    private final SaleController saleController;
    private final NotFound notFound;

    public ApiDispatcher(ClientController clientController,
                         ProductController productController,
                         SaleController saleController,
                         NotFound notFound) {

        this.clientController = clientController;
        this.productController = productController;
        this.saleController = saleController;
        this.notFound = notFound;
    }

    public Response toDispatcher(IHTTPSession session) {

        String uri = session.getUri();

        if (belongsTo(uri, CLIENTS_URI)) {
            return dispatchByMethod(session,
                    clientController::handleGetRequest,
                    clientController::handlePostRequest,
                    clientController::handleDeleteRequest);
        }

        if (belongsTo(uri, PRODUCTS_URI)) {
            return dispatchByMethod(session,
                    productController::handleGetRequest,
                    productController::handlePostRequest,
                    productController::handleDeleteRequest);
        }

        if (belongsTo(uri, SALES_URI)) {
            return dispatchByMethod(session,
                    saleController::handleGetRequest,
                    saleController::handlePostRequest,
                    saleController::handleDeleteRequest);
        }

        return notFound.handleRequest(session);
    }

    // Escolhe o handler do controller conforme o método HTTP.
    // Recebe os handlers como funções para que a mesma lógica sirva para todos os controllers.
    private Response dispatchByMethod(IHTTPSession session,
                                      Function<IHTTPSession, Response> getHandler,
                                      Function<IHTTPSession, Response> postHandler,
                                      Function<IHTTPSession, Response> deleteHandler) {

        Method method = session.getMethod();

        if (method == Method.GET) {
            return getHandler.apply(session);
        }

        if (method == Method.POST) {
            return postHandler.apply(session);
        }

        if (method == Method.DELETE) {
            return deleteHandler.apply(session);
        }

        return methodNotAllowed();
    }

    // A URI pertence ao recurso se for exatamente a base ou começar com "base/".
    // Evita que "/api/v1/salesXYZ" seja tratado como "/api/v1/sales".
    private boolean belongsTo(String uri, String baseUri) {
        return uri.equals(baseUri) || uri.startsWith(baseUri + "/");
    }

    private Response methodNotAllowed() {

        Response response = NanoHTTPD.newFixedLengthResponse(
                Status.METHOD_NOT_ALLOWED,
                "application/json; charset=UTF-8",
                "{\"error\": \"Método não permitido. Use " + ALLOWED_METHODS + ".\"}"
        );

        // O cabeçalho Allow é o padrão HTTP para informar quais métodos são aceitos.
        response.addHeader("Allow", ALLOWED_METHODS);
        return response;
    }
}