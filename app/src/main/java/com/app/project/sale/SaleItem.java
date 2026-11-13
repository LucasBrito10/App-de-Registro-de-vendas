package com.app.project.sale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.app.project.products.Product;

import java.math.BigDecimal;

// Tabela intermediária entre Sale e Product (relação N:N).
// Cada linha é "um produto dentro de uma venda", com a quantidade e o preço praticado.
@Entity(
    tableName = "sale_items",
    foreignKeys = {
        @ForeignKey(
            entity = Sale.class,
            parentColumns = "saleId",
            childColumns = "sale_id",
            onDelete = ForeignKey.CASCADE   // Apagou a venda, apaga seus itens
        ),
        @ForeignKey(
            entity = Product.class,
            parentColumns = "productId",
            childColumns = "product_id",
            onDelete = ForeignKey.SET_NULL  // Apagou o produto, a venda continua existindo (ver productName)
        )
    },
    indices = {
        @Index("sale_id"),
        @Index("product_id")
    }
)
public class SaleItem {

    @PrimaryKey(autoGenerate = true)
    public long saleItemId;

    @ColumnInfo(name = "sale_id")
    public long saleId;

    
    @ColumnInfo(name = "product_id")
    public Long productId;

    
    @ColumnInfo(name = "product_name")
    public String productName;

    public int quantity;

   
    @ColumnInfo(name = "unit_price")
    public BigDecimal unitPrice;
}
