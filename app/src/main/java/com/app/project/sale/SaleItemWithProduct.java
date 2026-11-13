package com.app.project.sale;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.app.project.products.Product;

// POJO: um item da venda junto com o produto do catálogo a que ele se refere.
public class SaleItemWithProduct {

    @Embedded
    public SaleItem item;

    // Será null se o produto tiver sido excluído do catálogo.
    // Nesse caso, use item.productName e item.unitPrice, que guardam os dados da época da venda.
    @Relation(
        parentColumn = "product_id",
        entityColumn = "productId"
    )
    public Product product;
}
