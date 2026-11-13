package com.app.project.server;

import android.content.res.AssetManager;
import android.util.Log;

import com.app.project.ApiDispatcher;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

// Recebe as requisições HTTP, aplica as verificações de segurança e encaminha:
//  - rotas /api/v1/  → ApiDispatcher (controllers)
//  - demais rotas    → arquivos estáticos do frontend (assets/www)

// Não há cabeçalhos CORS: frontend e API estão na mesma origem, então o CORS não é necessário,
// e sem ele o navegador impede que outros sites leiam respostas ou enviem JSON para esta API.
//A ideia é manter assim, já que o app é offline para a minha mãe. Quem sabe mudar depois.
public class NanoServer extends NanoHTTPD {

    private static final String TAG = "NanoServer";

    private static final String API_PREFIX = "/api/v1/";
    private static final String SESSION_COOKIE_NAME = "session";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String JSON_RESPONSE_TYPE = "application/json; charset=UTF-8";

    private final ApiDispatcher apiDispatcher;
    private final AssetManager assetManager;

    // Token de sessão gerado pela ProjectApplication. 
    private final byte[] sessionToken;

    // Valores esperados nos cabeçalhos Host e Origin
    private final String expectedHost;
    private final String expectedOrigin;

    public NanoServer(String hostName, int port, ApiDispatcher apiDispatcher,
                      AssetManager assetManager, String sessionToken) {
        super(hostName, port);

        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new IllegalArgumentException("O token de sessão é obrigatório.");
        }

        this.apiDispatcher = apiDispatcher;
        this.assetManager = assetManager;
        this.sessionToken = sessionToken.getBytes(StandardCharsets.UTF_8);
        this.expectedHost = hostName + ":" + port;
        this.expectedOrigin = "http://" + this.expectedHost;
    }

    @Override
    public Response serve(IHTTPSession session) {

        try {
            
            Map<String, String> headers = session.getHeaders();
            String uri = session.getUri();

            //Sequência de verificações de cabeçalhos e informações que a requisição enviada ao servidor fornece
            //Verifica host,content-type,token... 
            if (!this.expectedHost.equals(headers.get("host"))) {
                return errorResponse(Status.FORBIDDEN, "Host não permitido.");
            }

            if (!uri.startsWith(API_PREFIX)) {
                return handleStaticAsset(uri);
            }

            // Origin: navegadores enviam este cabeçalho em POST/PUT/DELETE (e em chamadas
            //    entre origens). Se veio, tem que ser o próprio app. Se não veio, segue para o token.
            String origin = headers.get("origin");
            if (origin != null && !this.expectedOrigin.equals(origin)) {
                return errorResponse(Status.FORBIDDEN, "Origem não permitida.");
            }

            // Token de sessão.
            if (!isAuthenticated(session)) {
                return errorResponse(Status.UNAUTHORIZED, "Não autorizado.");
            }

            // Content-Type nas rotas de escrita.
            if (requiresJsonBody(session.getMethod()) && !isJsonContentType(headers.get("content-type"))) {
                return errorResponse(Status.UNSUPPORTED_MEDIA_TYPE, "O corpo da requisição deve ser application/json.");
            }

            Response response = this.apiDispatcher.toDispatcher(session);

            // se algum controller devolver null, responde com erro em vez de
            // deixar o NanoHTTPD gerar uma resposta genérica.
            if (response == null) {
                return errorResponse(Status.NOT_FOUND, "Endpoint não encontrado.");
            }

            return response;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar " + session.getMethod() + " " + session.getUri(), e);
            return errorResponse(Status.INTERNAL_ERROR, "Falha interna no servidor HTTP.");
        }
    }

    

    // Confere se a requisição trouxe o cookie de sessão com o token correto.
    private boolean isAuthenticated(IHTTPSession session) {

        String receivedToken = session.getCookies().read(SESSION_COOKIE_NAME);

        if (receivedToken == null) {
            return false;
        }

        // MessageDigest.isEqual compara em tempo constante: o tempo de resposta não revela
        // quantos caracteres do token estavam certos (proteção contra ataque de temporização).
        return MessageDigest.isEqual(this.sessionToken, receivedToken.getBytes(StandardCharsets.UTF_8));
    }

    // Métodos que enviam corpo. DELETE não entra, não tem corpo nas rotas desta API.
    private boolean requiresJsonBody(Method method) {
        return method == Method.POST || method == Method.PUT || method == Method.PATCH;
    }

    // Aceita "application/json" com ou sem parâmetros
    private boolean isJsonContentType(String contentType) {

        if (contentType == null) {
            return false;
        }

        String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return JSON_CONTENT_TYPE.equals(mediaType);
    }


    private Response handleStaticAsset(String uri) {

        if (uri.equals("/")) {
            uri = "/index.html";
        }

        try {
            // Tenta servir o arquivo estático real (js, css, png...).
            InputStream fileStream = this.assetManager.open("www" + uri);
            return newChunkedResponse(Status.OK, getMimeType(uri), fileStream);

        } catch (Exception e) {
            // SPA FALLBACK: se o arquivo não existe (ex.: rota /clientes/novo do React Router),
            // devolve o index.html para o frontend assumir o controle da navegação.
            try {
                InputStream indexStream = this.assetManager.open("www/index.html");
                return newChunkedResponse(Status.OK, "text/html", indexStream);
            } catch (Exception fatal) {
                return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Frontend build não encontrado.");
            }
        }
    }

    private String getMimeType(String uri) {

        if (uri.endsWith(".html")) return "text/html";
        if (uri.endsWith(".js")) return "application/javascript";
        if (uri.endsWith(".css")) return "text/css";
        if (uri.endsWith(".png")) return "image/png";
        if (uri.endsWith(".svg")) return "image/svg+xml";
        if (uri.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

   

    // As mensagens são fixas (escritas aqui no código), por isso podem ser concatenadas no JSON
    // sem escape. Não use este método com texto vindo da requisição.
    private Response errorResponse(Status status, String message) {
        return newFixedLengthResponse(status, JSON_RESPONSE_TYPE, "{\"error\": \"" + message + "\"}");
    }
}