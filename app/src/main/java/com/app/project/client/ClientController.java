package com.app.project.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientController {

    private ClientService clientService;
    private Gson gson;

    // Regex para capturar IDs na URI
    private static final Pattern CLIENT_BY_ID_PATTERN = Pattern.compile("^/api/v1/clients/(\\d+)$");

    public ClientController(ClientService clientService, Gson gson) {
        this.clientService = clientService;
        this.gson = gson;
    }

    public Response handleGetRequest(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, List<String>> params = session.getParameters();

        try {

            Matcher matcher = CLIENT_BY_ID_PATTERN.matcher(uri);
            if (matcher.matches()) {
                long clientId = Long.parseLong(matcher.group(1));
                var client = clientService.getClientById(clientId);

                if (client == null) {
                    return errorResponse(Status.NOT_FOUND, "Cliente não encontrado");
                }
                return jsonResponse(Status.OK, client);
            }

            if (uri.equals("/api/v1/clients")) {

                if (params.containsKey("name")) {
                    String clientName = params.get("name").get(0);
                    var clients = clientService.getClientByName(clientName);
                    return jsonResponse(Status.OK, clients);
                }

                if (params.containsKey("phone")) {
                    String clientPhone = params.get("phone").get(0);
                    var clients = clientService.getClientByPhone(clientPhone);
                    return jsonResponse(Status.OK, clients);
                }

                if (params.containsKey("page") || params.containsKey("size")) {
                    int page = Integer.parseInt(params.getOrDefault("page", List.of("1")).get(0));
                    int size = Integer.parseInt(params.getOrDefault("size", List.of("10")).get(0));

                    if (page < 1 || size < 1) {
                        return errorResponse(Status.BAD_REQUEST, "Os parâmetros page e size devem ser maiores que zero.");
                    }

                    var paginatedClients = clientService.getClientsPaginated(page, size);
                    return jsonResponse(Status.OK, paginatedClients);
                }

                // Se não houver nenhum parâmetro, retorna todos os clientes
                var allClients = clientService.getAllClients();
                return jsonResponse(Status.OK, allClients);
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint GET não encontrado");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "Parâmetro numérico inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar requisição.");
        }
    }

    public Response handlePostRequest(IHTTPSession session) {
        String uri = session.getUri();
        boolean isCollection = "/api/v1/clients".equals(uri);
        Matcher matcher = CLIENT_BY_ID_PATTERN.matcher(uri);
        boolean isSpecific = matcher.matches();

        if (isCollection || isSpecific) {
            try {

                //Extraindo o corpo da requisição e passando o parsebody exigido pelo NanoHTTPD
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String postBody = files.get("postData");


                //Verificação de corpo da requisição vazio ou nulo
                if (postBody == null || postBody.trim().isEmpty()) {
                    return errorResponse(Status.BAD_REQUEST, "O corpo da requisição não pode estar vazio.");
                }

                ClientDto clientRequest = gson.fromJson(postBody, ClientDto.class);

                if (clientRequest == null) {
                    return errorResponse(Status.BAD_REQUEST, "O corpo da requisição não pode estar vazio.");
                }

                if (isSpecific) {
                    long clientId = Long.parseLong(matcher.group(1));
                    var updatedClient = clientService.updateClient(clientId, clientRequest);

                    if (updatedClient == null) {
                        return errorResponse(Status.NOT_FOUND, "Cliente não encontrado");
                    }
                    return jsonResponse(Status.OK, updatedClient);
                }

                return jsonResponse(Status.CREATED, clientService.createClient(clientRequest));

            } catch (IOException e) {
                // Erro ao ler a requisição
                return errorResponse(Status.INTERNAL_ERROR, "Erro de I/O ao processar a requisição.");

            } catch (NanoHTTPD.ResponseException e) {
                // Erro gerado pelo próprio NanoHTTPD
                return errorResponse(e.getStatus(), e.getMessage());

            } catch (JsonSyntaxException e) {
                return errorResponse(Status.BAD_REQUEST, "JSON mal formatado.");

            } catch (NumberFormatException e) {
                return errorResponse(Status.BAD_REQUEST, "ID de cliente inválido.");

            } catch (Exception e) {
                // Erro de parse do JSON (ex: JsonSyntaxException) ou falha no banco
                e.printStackTrace();
                return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar a requisição.");
            }
        }

        // O método é POST, mas a URI não corresponde a nenhuma rota conhecida
        return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");
    }

    private Response jsonResponse(Status status, Object body) {
        return NanoHTTPD.newFixedLengthResponse(status, "application/json", gson.toJson(body));
    }

    private Response errorResponse(Status status, String message) {
        String safeMessage = message != null ? message : "Erro ao processar a requisição.";
        return jsonResponse(status, Map.of("error", safeMessage));
    }
}