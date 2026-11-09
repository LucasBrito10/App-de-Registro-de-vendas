package com.app.project.products;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.math.BigDecimal;

@Entity(tableName = "products")
public class Product {
    
    @PrimaryKey(autoGenerate = true)
    public long productId;
    
    public String name;
    
    // Requer o mesmo TypeConverter de BigDecimal configurado para a classe Sale.
    public BigDecimal price;
}