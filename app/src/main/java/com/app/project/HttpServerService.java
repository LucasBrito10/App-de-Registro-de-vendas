package com.app.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;

import com.app.project.client.ClientController;
import com.app.project.client.ClientDao;
import com.app.project.client.ClientRepository;
import com.app.project.client.ClientService;
import com.app.project.products.ProductController;
import com.app.project.products.ProductDao;
import com.app.project.products.ProductRepository;
import com.app.project.products.ProductService;
import com.app.project.sale.SaleController;
import com.app.project.sale.SaleDao;
import com.app.project.sale.SaleRepository;
import com.app.project.sale.SaleService;
import com.app.project.server.NanoServer;
import com.app.project.shared.NotFound;

import java.io.IOException;

// Cria as instâncias de objetos relacionados ao servidor HTTP e ao backend.
// Tem um papel parecido com o Spring Boot no que diz respeito à injeção de dependências
// e ao gerenciamento do ciclo de vida dos objetos.
public class HttpServerService extends Service {

    private static final String TAG = "HttpServerService";
    private static final String CHANNEL_ID = "HttpServerChannel";
    private static final int NOTIFICATION_ID = 1;

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    private NanoServer nanoServer;

    @Override
    public void onCreate() {

        super.onCreate();
        setupForegroundNotification();

        ApiDispatcher apiDispatcher = injectDependencies();
        AssetManager assetManager = getAssets();

        // O token foi gerado pela ProjectApplication antes deste serviço ser iniciado.
        // Este serviço apenas o repassa ao NanoServer, que é quem guarda e valida o token
        // em cada requisição. Por isso não há atributo para o token nesta classe.
        String sessionToken = ((ProjectApplication) getApplication()).getSessionToken();

        this.nanoServer = new NanoServer(HOST, PORT, apiDispatcher, assetManager, sessionToken);

        try {
            this.nanoServer.start();
        } catch (IOException e) {
            // Sem servidor o app não funciona: registra o erro e encerra o serviço.
            Log.e(TAG, "Falha ao iniciar o servidor HTTP na porta " + PORT, e);
            stopSelf();
        }
    }

    // Aqui as dependências são criadas e injetadas (DAOs → Repositories → Services → Controllers → Dispatcher).
    private ApiDispatcher injectDependencies() {

        AppDatabase db = AppDatabase.getInstance(this);

        ClientDao clientDao = db.clientDao();
        ProductDao productDao = db.productDao();
        SaleDao saleDao = db.saleDao();

        ClientRepository clientRepository = new ClientRepository(clientDao);
        ProductRepository productRepository = new ProductRepository(productDao);
        SaleRepository saleRepository = new SaleRepository(saleDao);

        ClientService clientService = new ClientService(clientRepository);
        ProductService productService = new ProductService(productRepository);
        // A venda consulta clientes (existe?) e produtos (nome e preço atuais).
        SaleService saleService = new SaleService(saleRepository, clientRepository, productRepository);

        ClientController clientController = new ClientController(clientService);
        ProductController productController = new ProductController(productService);
        SaleController saleController = new SaleController(saleService);
        NotFound notFound = new NotFound();

        return new ApiDispatcher(clientController, productController, saleController, notFound);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.nanoServer != null) {
            this.nanoServer.stop();
            this.nanoServer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Serviço iniciado (started), não vinculado (bound): nenhuma Activity se conecta a ele diretamente.
        return null;
    }

    // Exigência do Android para manter o servidor vivo em segundo plano.
    
    private void setupForegroundNotification() {

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Servidor local",
                NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Registro de Vendas")
                .setContentText("Servidor local em execução")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }
}