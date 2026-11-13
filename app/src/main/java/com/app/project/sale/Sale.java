package com.app.project.sale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.app.project.client.Client;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Entity(
    tableName = "sales",
    foreignKeys = @ForeignKey(
        entity = Client.class,
        parentColumns = "clientId",
        childColumns = "client_id",
        onDelete = ForeignKey.CASCADE // Apaga as vendas se o cliente for apagado
    ),
    indices = {
        @Index("client_id"),      // Índice obrigatório para a chave estrangeira
        @Index("purchase_date")   // Acelera filtros e ordenação por data
    }
)

public class Sale {

    @PrimaryKey(autoGenerate = true)
    public long saleId;

    @ColumnInfo(name = "client_id")
    public long clientId;

    
    @ColumnInfo(name = "purchase_date")
    public LocalDate purchaseDate;

    
    @ColumnInfo(name = "created_at")
    public Instant createdAt;

    
    @ColumnInfo(name = "total_price")
    public BigDecimal totalPrice;

}
