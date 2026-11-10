package com.app.project.client;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.app.project.client.Client;
import com.app.project.sale.Sale;
import java.util.List;

// Implementação do POJO para permitir uma busca de clientes mais eficiente, 
// trazendo também as vendas associadas a cada cliente.
public class ClientWithSales {
    
    @Embedded
    public Client client;
    
    @Relation(
        parentColumn = "clientId",
        entityColumn = "client_id"
    )
    public List<Sale> sales;
}