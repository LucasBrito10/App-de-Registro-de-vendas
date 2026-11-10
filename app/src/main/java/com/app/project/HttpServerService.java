package com.app.project;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.app.project.server.NanoServer;
import com.app.project.client.ClientController;
import com.app.project.client.ClientService;
import com.app.project.client.ClientRepository;
import com.app.project.products.ProductController;
import com.app.project.sale.SaleController;
import com.app.project.sale.SaleService;
import com.app.project.sale.SaleRepository;
import com.app.project.products.ProductRepository;
import com.app.project.products.ProductService;
import com.app.project.shared.NotFound;
import android.content.res.AssetManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import java.io.IOException;
import com.app.project.client.ClientDao;
import com.app.project.sale.SaleDao;
import com.app.project.products.ProductDao;


// Cria as instancias de objetos relacionados ao servidor HTTP e ao backend. 
//Tem um papel parecido com spring boot no que diz respeito a injeção de dependências e gerenciamento de ciclo de vida dos objetos.

public class HttpServerService extends Service {

    private NanoServer nanoServer;
    private static final String CHANNEL_ID = "HttpServerChannel";

    public void onCreate() {

        super.onCreate();
        setupForegroundNotification();

        ApiDispatcher apiDispatcher = injectDependencies();

        AssetManager assetManager = getAssets(); // Obtém o AssetManager do contexto do serviço

        this.nanoServer = new NanoServer("127.0.0.1", 8080, apiDispatcher, assetManager);

        try {
            this.nanoServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

        private ApiDispatcher injectDependencies() {

        //Aqui as dependências serão injetadas(Controllers,Services,Repositories,API) e o servidor HTTP será inicializado.

        
       
        AppDatabase db = AppDatabase.getInstance(this);

        ClientDao clientDao = db.clientDao();
        ProductDao productDao = db.productDao();
        SaleDao saleDao = db.saleDao();


        ClientRepository clientRepository = new ClientRepository(clientDao);
        ProductRepository productRepository = new ProductRepository(productDao);
        SaleRepository saleRepository = new SaleRepository(saleDao);
       
        ClientService clientService = new ClientService(clientRepository);
        ProductService productService = new ProductService(productRepository);
        SaleService saleService = new SaleService(saleRepository);
       
        ClientController clientsController = new ClientController(clientService);
        ProductController productsController = new ProductController(productService);
        SaleController salesController = new SaleController(saleService);
        NotFound notFound = new NotFound();

        
        
        return new ApiDispatcher(clientsController, productsController, salesController, notFound);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.nanoServer != null) {
            this.nanoServer.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        
        return null; 
    }

    private void setupForegroundNotification() {
        // Exigência do Android para manter o servidor vivo em background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, 
                    "Local Server", 
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Local Server")
                    .setContentText("Servidor local a correr...")
                    .build();
                    
            startForeground(1, notification);
        }
    }




}