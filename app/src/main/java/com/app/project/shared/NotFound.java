package com.app.project.shared;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

// Resposta padrão para rotas da API que não pertencem a nenhum recurso.
public class NotFound {

    public Response handleRequest(IHTTPSession session) {

        return NanoHTTPD.newFixedLengthResponse(
                Status.NOT_FOUND,
                "application/json; charset=UTF-8",
                "{\"error\": \"Endpoint não encontrado.\"}"
        );
    }
}