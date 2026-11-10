package com.app.project.client;

import com.app.project.client.ClientDto;
import com.app.project.sale.Sale; 
import java.util.List;

public class ClientWithSalesDto {
    private ClientDto client;
    private List<Sale> sales;

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }

    public List<Sale> getSales() {
        return sales;
    }

    public void setSales(List<Sale> sales) {
        this.sales = sales;
    }
}
