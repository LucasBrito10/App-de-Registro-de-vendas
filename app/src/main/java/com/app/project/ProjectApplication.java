package com.app.project;

import android.app.Application;
import android.content.Intent;

import java.security.SecureRandom;
import java.util.Base64;

// A classe ProjectApplication é a classe principal do aplicativo Android.
// Ela estende a classe Application, que é a base para manter o estado global do aplicativo.
public class ProjectApplication extends Application {

    
    private static final int TOKEN_SIZE_BYTES = 32;

    // Token de sessão gerado a cada vez que o processo do app é criado.
    // Vive apenas em memória. Nunca é salvo em disco nem exposto por endpoint HTTP.
    private String sessionToken;

    @Override
    public void onCreate() {

        super.onCreate();

        // 1. Gera o token ANTES de iniciar o servidor, para que o HttpServerService
        //    e a MainActivity já o encontrem pronto quando forem buscá-lo.
        this.sessionToken = generateSessionToken();

        // 2. Dispara o serviço do servidor NanoHTTPD.
        // Como o minSdk do projeto é 26 (Android Oreo), startForegroundService() está sempre disponível.
        Intent serviceIntent = new Intent(this, HttpServerService.class);
        startForegroundService(serviceIntent);
    }

    // Usado pelo HttpServerService (para o NanoServer validar as requisições)
    // e pela MainActivity (para gravar o cookie na WebView).
    public String getSessionToken() {
        return this.sessionToken;
    }

    private String generateSessionToken() {

        // SecureRandom usa a fonte de aleatoriedade criptográfica do sistema.
        byte[] randomBytes = new byte[TOKEN_SIZE_BYTES];
        new SecureRandom().nextBytes(randomBytes);

        // Base64 "URL-safe" sem padding gera apenas letras, números, '-' e '_',
        // caracteres seguros para usar como valor de cookie.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}