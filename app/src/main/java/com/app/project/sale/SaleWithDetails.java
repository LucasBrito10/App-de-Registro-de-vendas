package com.app.project.sale;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

// POJO: a venda completa, com os produtos vendidos e as parcelas.
public class SaleWithDetails {

    @Embedded
    public Sale sale;

    // Relação aninhada: cada SaleItem já vem com o seu Product.
    // O "entity" indica de qual tabela vêm os filhos, já que o tipo da lista é um POJO.
    @Relation(
        entity = SaleItem.class,
        parentColumn = "saleId",
        entityColumn = "sale_id"
    )
    public List<SaleItemWithProduct> items;

    @Relation(
        parentColumn = "saleId",
        entityColumn = "sale_id"
    )
    public List<Installment> installments;
}
