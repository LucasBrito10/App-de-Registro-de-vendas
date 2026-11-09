package com.app.project.sale;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import com.app.project.client.Client;
import java.math.BigDecimal;

@Entity(
    tableName = "sales",
    foreignKeys = @ForeignKey(
        entity = Client.class,
        parentColumns = "clientId",
        childColumns = "client_id",
        onDelete = ForeignKey.CASCADE // Apaga as vendas se o cliente for apagado
    ),
    indices = {@Index("client_id")} // Índice obrigatório para a chave estrangeira
)
public class Sale {

    @PrimaryKey(autoGenerate = true)
    public long saleId;
    
    @ColumnInfo(name = "client_id")
    public long clientId;
    
    public int quantity;
    public BigDecimal totalPrice;
}