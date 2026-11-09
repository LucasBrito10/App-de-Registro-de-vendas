package com.app.project;


import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import com.app.project.client.ClientController;
import com.app.project.products.ProductController;
import com.app.project.sale.SaleController;
import com.app.project.shared.NotFound;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 
import com.app.project.client.ClientController;




public class ApiDispatcher {


    private ClientController clientController;
    private ProductController productController;
    private SaleController saleController;
    private NotFound notFound;

    
    public ApiDispatcher(ClientController clientController, 
    ProductController productsController, SaleController saleController, NotFound notFound) {
        
        this.clientController = clientController;
        this.productController = productsController;
        this.saleController = saleController;
        this.notFound = notFound;
    
    }

    public Response toDispatcher(IHTTPSession session) {
        

        if(session.getMethod() == NanoHTTPD.Method.GET) {

            if(session.getUri().startsWith("/api/v1/clients")) {
                return clientController.handleGetRequest(session);
            } else if(session.getUri().startsWith("/api/v1/products")) {
                return productController.handleGetRequest(session);
            } else if(session.getUri().startsWith("/api/v1/sales")) {
                return saleController.handleGetRequest(session);
            } else {
                return notFound.handleRequest(session);
            }



        }

        return notFound.handleRequest(session);
    }

    



    
}
