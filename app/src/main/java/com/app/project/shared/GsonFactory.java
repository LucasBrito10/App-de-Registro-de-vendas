package com.app.project.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// Cria o Gson usado por todos os controllers, já configurado para os tipos de data do java.time.
// Sem isso, o Gson serializaria LocalDate como {"year":2026,"month":11,"day":13}.
public final class GsonFactory {

    private GsonFactory() {
    }

    public static Gson create() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (json, type, ctx) -> {
                    try {
                        return LocalDate.parse(json.getAsString());
                    } catch (DateTimeParseException e) {
                        throw new JsonParseException("Data inválida, use o formato AAAA-MM-DD: " + json.getAsString(), e);
                    }
                })
            .registerTypeAdapter(Instant.class,
                (JsonSerializer<Instant>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Instant.class,
                (JsonDeserializer<Instant>) (json, type, ctx) -> {
                    try {
                        return Instant.parse(json.getAsString());
                    } catch (DateTimeParseException e) {
                        throw new JsonParseException("Data/hora inválida: " + json.getAsString(), e);
                    }
                })
            .create();
    }
}
