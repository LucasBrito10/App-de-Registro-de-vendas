package com.app.project.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoHTTPD.Method;
import android.content.res.AssetManager;
import java.io.InputStream;
import com.app.project.ApiDispatcher;

// A ideia é que esta classe seja responsável por receber as requisições HTTP e encaminhá-las para os controladores apropriados, 
//com a ajuda da ApiDispatcher. 
public class NanoServer extends NanoHTTPD{

    private ApiDispatcher apiDispatcher;
    private AssetManager assetManager;


    //Criar uma nova instância do servidor com a porta especificada 
    public NanoServer(String hostName,int port, ApiDispatcher apiDispatcher, AssetManager assetManager) {
        super(hostName, port);
        this.apiDispatcher = apiDispatcher;
        this.assetManager = assetManager;

    }

    @Override
    public Response serve(IHTTPSession session) {
        
        Response response;

        if(session.getMethod() == Method.OPTIONS) {
            return getCorsPreflightResponse();
        }
        
        try{
            if(session.getUri().startsWith("/api/v1/")) {
                response = this.apiDispatcher.toDispatcher(session);
            }
            else {
                response = handleStaticAsset(session.getUri());
            }

            return appendCorsHeaders(response);
        }
        catch (Exception e) {
            
            e.printStackTrace();
            return appendCorsHeaders(newFixedLengthResponse(
                Status.INTERNAL_ERROR, 
                "application/json", 
                "{\"error\": \"Falha interna no servidor HTTP\"}"
            ));
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


    private Response handleStaticAsset(String uri) {
        if (uri.equals("/")) {
            uri = "/index.html";
        }

        try {
            // Tenta servir o ficheiro estático real (js, css, png)
            InputStream fileStream = assetManager.open("www" + uri);
            return NanoHTTPD.newChunkedResponse(Status.OK, getMimeType(uri), fileStream);

        } catch (Exception e) {
            // SPA FALLBACK: Se o asset não existe (ex: rota /clientes/novo do React Router),
            // devolve o index.html para o frontend assumir o controle da navegação.
            try {
                InputStream indexStream = assetManager.open("www/index.html");
                return NanoHTTPD.newChunkedResponse(Status.OK, "text/html", indexStream);
            } catch (Exception fatal) {
                return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Frontend build não encontrado.");
            }
        }
    }


    private Response getCorsPreflightResponse() {
        // Devolve código 200 OK vazio e rápido apenas para satisfazer o navegador
        Response response = newFixedLengthResponse(Status.OK, "text/plain", "");
        return appendCorsHeaders(response);
    }

    private Response appendCorsHeaders(Response response) {
        if (response != null) {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Headers", "origin, accept, content-type, authorization");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
        return response;
    }



} 