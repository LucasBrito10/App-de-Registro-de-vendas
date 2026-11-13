package com.app.project.sale;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.app.project.products.Product;

import java.util.List;

// venda com a lista de produtos, usando SaleItem como tabela intermediária.
// Traz apenas os Product (sem quantidade nem preço cobrado). Para isso, use SaleWithDetails.
public class SaleWithProducts {

    @Embedded
    public Sale sale;

    @Relation(
        parentColumn = "saleId",           
        entityColumn = "productId",        
        associateBy = @Junction(
            value = SaleItem.class,        
            parentColumn = "sale_id",      
            entityColumn = "product_id"    
        )
    )
    public List<Product> products;
}
