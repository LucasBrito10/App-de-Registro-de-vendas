package com.app.project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;


//Classe responsável por exibir a interface do usuário do aplicativo, que é baseada em uma WebView. 
//Ela herda de WebViewActivity, que é uma classe personalizada que encapsula a funcionalidade da WebView.

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    //O método onCreate é chamado quando a activity é criada. Ele configura a WebView, define o conteúdo da atividade e carrega a URL especificada.
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //O savedInstanceState é um objeto Bundle que contém o estado anterior da atividade, se houver. Ele é passado para o método onCreate para que a atividade possa restaurar seu estado anterior, se necessário.
        super.onCreate(savedInstanceState);

        this.webView = new WebView(this);
        setContentView(this.webView);
        configureWebView(this.webView);
        setupBackNavigation(this.webView);

        this.webView.loadUrl("http://127.0.0.1:8080");
    }

    private void configureWebView(WebView webView) {
        
        // Habilita o JavaScript na WebView, permitindo que scripts sejam executados nas páginas carregadas.
        webView.getSettings().setJavaScriptEnabled(true);
        // Habilita o armazenamento local (DOM Storage) na WebView, permitindo que as páginas armazenem dados localmente.
        webView.getSettings().setDomStorageEnabled(true);
        // Permite o acesso a arquivos locais na WebView, permitindo que as páginas carreguem recursos locais.
        webView.getSettings().setAllowFileAccess(true);
        // Permite o acesso a conteúdo local na WebView, permitindo que as páginas carreguem recursos locais.
        webView.getSettings().setAllowContentAccess(true);

        //Proíbe o carregamento de URLs externas na WebView, garantindo que apenas URLs internas sejam carregadas.
        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }


    //Condfigura a possibilidade de voltar para a tela anterior do frontend ao pressionar o botão físico de voltar do Android.
    private void setupBackNavigation(WebView webView) {
        
        //O método getOnBackPressedDispatcher() retorna um objeto OnBackPressedDispatcher, 
        //que é responsável por gerenciar os eventos de pressionamento do botão de voltar. 
        //O método addCallback() adiciona um callback personalizado para lidar com o evento de pressionamento do botão de voltar.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            
            @Override
            public void handleOnBackPressed() {
                // Se o React Router tiver histórico, volta para a tela anterior do frontend
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // Se não houver histórico, desativa o bloqueio e fecha o app
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.webView != null) {
            this.webView.destroy();
            this.webView = null;
        }
    }
}
