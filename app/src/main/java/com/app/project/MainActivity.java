package com.app.project;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

// Classe responsável por exibir a interface do usuário do aplicativo, que é baseada em uma WebView.
// O frontend (SPA) é servido pelo servidor local NanoHTTPD em http://127.0.0.1:8080.
public class MainActivity extends AppCompatActivity {

    private static final String APP_URL = "http://127.0.0.1:8080";
    private static final String APP_HOST = "127.0.0.1";
    private static final int APP_PORT = 8080;

    // Nome do cookie que o NanoServer espera em toda requisição para /api/v1/.
    private static final String SESSION_COOKIE_NAME = "session";

    private WebView webView;

    // O método onCreate é chamado quando a Activity é criada.
    // Ele cria e configura a WebView, grava o cookie de sessão e só então carrega o frontend.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.webView = new WebView(this);
        setContentView(this.webView);
        configureWebView(this.webView);
        setupBackNavigation(this.webView);

        // A página só é carregada depois que o cookie estiver gravado,
        // garantindo que as primeiras chamadas à API já saiam autenticadas.
        setupSessionCookie(() -> {
            if (this.webView != null) {
                this.webView.loadUrl(APP_URL);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView webView) {

        WebSettings settings = webView.getSettings();

        // O frontend é uma SPA: precisa de JavaScript e de armazenamento local (DOM Storage).
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // O frontend é servido via HTTP pelo NanoServer, não por arquivos locais.
        // Desligar esses acessos reduz o que um script malicioso conseguiria alcançar.
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        // Mantém dentro da WebView apenas a navegação para o servidor local.
        // Qualquer outra URL é bloqueada (retornar true = "eu tratei, não carregue").
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !isInternalUrl(request.getUrl());
            }
        });
    }

    private boolean isInternalUrl(Uri uri) {
        return "http".equals(uri.getScheme())
                && APP_HOST.equals(uri.getHost())
                && uri.getPort() == APP_PORT;
    }

    // Lê o token gerado pela ProjectApplication e o grava como cookie da WebView.
    // O token não fica guardado em atributo desta classe: a partir daqui quem o guarda é o
    // CookieManager, e quem o envia em cada requisição é o próprio motor da WebView.
    private void setupSessionCookie(Runnable onReady) {

        String sessionToken = ((ProjectApplication) getApplication()).getSessionToken();

        // HttpOnly: o JavaScript da página não consegue ler o cookie (proteção contra scripts maliciosos).
        // SameSite=Strict: o navegador não envia o cookie em requisições vindas de outros sites.
        String cookie = SESSION_COOKIE_NAME + "=" + sessionToken
                + "; Path=/; HttpOnly; SameSite=Strict";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        // A versão com callback é assíncrona: o Runnable só roda depois que o cookie foi gravado.
        cookieManager.setCookie(APP_URL, cookie, success -> {
            cookieManager.flush();
            onReady.run();
        });
    }

    // Configura o botão físico "voltar" do Android para navegar no histórico do frontend.
    private void setupBackNavigation(WebView webView) {

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Se o React Router tiver histórico, volta para a tela anterior do frontend.
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // Se não houver histórico, desativa este callback e deixa o Android fechar a tela.
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (this.webView != null) {
            this.webView.destroy();
            this.webView = null;
        }
        super.onDestroy();
    }
}