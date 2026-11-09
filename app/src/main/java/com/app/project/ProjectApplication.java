package com.app.project;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

//A classe AvaliaApplication é a classe principal do aplicativo Android. 
//Ela estende a classe Application, que é a base para manter o estado global do aplicativo.
public class ProjectApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Dispara o serviço do servidor NanoHTTPD assim que o app nasce
        Intent serviceIntent = new Intent(this, HttpServerService.class);
        
        // inicia um serviço em primeiro plano para a classe HttpServerService, que é responsável por gerenciar o servidor HTTP.
        // O método startForegroundService() é usado para iniciar o serviço em primeiro plano em dispositivos com Android Oreo (API 26) ou superior, 
        //enquanto o método startService() é usado para versões anteriores do Android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}