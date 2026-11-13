package com.app.project.client;

import com.app.project.shared.GsonFactory;
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

    private final ClientService clientService;
    private final Gson gson;

    private static final String BASE_URI = "/api/v1/clients";
    private static final Pattern CLIENT_BY_ID_PATTERN = Pattern.compile("^" + BASE_URI + "/(\\d+)$");

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
        this.gson = GsonFactory.create();
    }

    public Response handleGetRequest(IHTTPSession session) {
        
        String uri = session.getUri();
        Map<String, List<String>> params = session.getParameters();

        try {
            Matcher matcher = CLIENT_BY_ID_PATTERN.matcher(uri);
            if (matcher.matches()) {
                
                return fetchClientById(matcher.group(1), params);
            }

            if (BASE_URI.equals(uri)) {
                return fetchClients(params);
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
            Matcher matcher = CLIENT_BY_ID_PATTERN.matcher(uri);

            if (BASE_URI.equals(uri)) {
                return createClient(session);
            } 
            
            if (matcher.matches()) {
                
                return updateClient(session, matcher.group(1));
            }

            return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");

        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "ID de cliente inválido.");
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
            
            Matcher matcher = CLIENT_BY_ID_PATTERN.matcher(session.getUri());
            
            if (!matcher.matches()) {
                return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");
            }
            
            long id = Long.parseLong(matcher.group(1));
            
            boolean removed = clientService.deleteClient(id);
            
            if (!removed) {
                return errorResponse(Status.NOT_FOUND, "Cliente não encontrado.");
            }
            
            return NanoHTTPD.newFixedLengthResponse(Status.NO_CONTENT, "application/json; charset=UTF-8", "");
        
        } catch (NumberFormatException e) {
            return errorResponse(Status.BAD_REQUEST, "ID de cliente inválido.");
        
        } catch (Exception e) {
            e.printStackTrace();
            return errorResponse(Status.INTERNAL_ERROR, "Erro interno ao processar a requisição.");
        
        }

    }

    

    private Response fetchClientById(String idParam, Map<String, List<String>> params) {
        
        long clientId = Long.parseLong(idParam);
        
        
        boolean withSales = false;
        if (params.containsKey("withSales") && !params.get("withSales").isEmpty()) {
            withSales = "true".equalsIgnoreCase(params.get("withSales").get(0));
        }

        
        if (withSales) {
            var clientWithSales = clientService.getClientWithSalesById(clientId);
            
            if (clientWithSales == null) {
                return errorResponse(Status.NOT_FOUND, "Cliente não encontrado.");
            }
            return jsonResponse(Status.OK, clientWithSales);
        }

        
        var client = clientService.getClientById(clientId);
        
        if (client == null) {
            return errorResponse(Status.NOT_FOUND, "Cliente não encontrado.");
        }
        return jsonResponse(Status.OK, client);
    }

    private Response fetchClients(Map<String, List<String>> params) {
    
        String clientName = null;
        if (params.containsKey("name") && !params.get("name").isEmpty()) {
            clientName = params.get("name").get(0);
        }

    
        String clientPhone = null;
        if (params.containsKey("phone") && !params.get("phone").isEmpty()) {
            clientPhone = params.get("phone").get(0);
        }

        
        if (params.containsKey("page") || params.containsKey("size")) {
            try {
                int page = Integer.parseInt(params.getOrDefault("page", List.of("1")).get(0));
                int size = Integer.parseInt(params.getOrDefault("size", List.of("10")).get(0));

                if (page < 1 || size < 1) {
                    return errorResponse(Status.BAD_REQUEST, "Os parâmetros page e size devem ser maiores que zero.");
                }

            
                return jsonResponse(Status.OK, clientService.getClientsPaginated(clientName, clientPhone, page, size));
                
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                return errorResponse(Status.BAD_REQUEST, "Parâmetros de paginação inválidos.");
            }
        }

    
    if (clientPhone != null) {
        return jsonResponse(Status.OK, clientService.getClientByPhone(clientPhone));
    }

    if (clientName != null) {
        return jsonResponse(Status.OK, clientService.getClientByName(clientName));
    }

    
    return jsonResponse(Status.OK, clientService.getAllClients());
}
    

    private Response createClient(IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        ClientDto clientRequest = parseRequestBody(session);

        ClientDto createdClient = clientService.createClient(clientRequest);
        if (createdClient == null) {
            return errorResponse(Status.INTERNAL_ERROR, "Não foi possível criar o cliente.");
        }

        return jsonResponse(Status.CREATED, createdClient);
    }

    private Response updateClient(IHTTPSession session, String idParam) throws IOException, NanoHTTPD.ResponseException {
        
        long clientId = Long.parseLong(idParam);
        ClientDto clientRequest = parseRequestBody(session);

        if (clientRequest.getName() == null && clientRequest.getPhone() == null) {
            return errorResponse(Status.BAD_REQUEST, "Informe ao menos um campo para atualizar.");
        }

        if (clientRequest.getName() != null && clientRequest.getName().trim().isEmpty()) {
            return errorResponse(Status.BAD_REQUEST, "O nome não pode ser vazio.");
        }

        var updatedClient = clientService.updateClient(clientId, clientRequest);
        if (updatedClient == null) {
            return errorResponse(Status.NOT_FOUND, "Cliente não encontrado");
        }

        return jsonResponse(Status.OK, updatedClient);
    }

    private ClientDto parseRequestBody(IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String postBody = files.get("postData");

        if (postBody == null || postBody.trim().isEmpty()) {
            throw new IllegalArgumentException("O corpo da requisição não pode estar vazio.");
        }

        ClientDto clientRequest = gson.fromJson(postBody, ClientDto.class);
        if (clientRequest == null) {
            throw new IllegalArgumentException("Corpo da requisição inválido ou vazio.");
        }

        return clientRequest;
    }

    

    private Response jsonResponse(Status status, Object body) {
        return NanoHTTPD.newFixedLengthResponse(status, "application/json; charset=UTF-8", gson.toJson(body));
    }

    private Response errorResponse(Status status, String message) {
        String safeMessage = message != null ? message : "Erro ao processar a requisição.";
        return jsonResponse(status, Map.of("error", safeMessage));
    }
}