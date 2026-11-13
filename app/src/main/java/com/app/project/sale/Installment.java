package com.app.project.sale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity(
    tableName = "installments",
    foreignKeys = @ForeignKey(
        entity = Sale.class,
        parentColumns = "saleId",
        childColumns = "sale_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = {"sale_id", "number"}, unique = true), // impede "parcela 2" duplicada na mesma venda
        @Index("due_date")                                     // acelera "parcelas vencidas/vencendo"
    }
)
public class Installment {

    @PrimaryKey(autoGenerate = true)
    public long installmentId;

    @ColumnInfo(name = "sale_id")
    public long saleId;

    public int number;               // 1, 2, 3...

    @ColumnInfo(name = "due_date")
    public LocalDate dueDate;

    public BigDecimal amount;        // valor devido

    @ColumnInfo(name = "paid_at")
    public LocalDate paidAt;         // null = não paga

    @ColumnInfo(name = "paid_amount")
    public BigDecimal paidAmount;    // null = não paga
}