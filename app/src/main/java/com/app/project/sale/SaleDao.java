package com.app.project.sale;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Dao
public interface SaleDao {

    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertSale(Sale sale);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertSaleItems(List<SaleItem> items);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertInstallments(List<Installment> installments);

    // Grava a venda, seus itens e suas parcelas numa única transação:
    // ou tudo é salvo, ou nada é 
    // Retorna o ID gerado para a venda.
    @Transaction
    default long insertSaleWithDetails(Sale sale, List<SaleItem> items, List<Installment> installments) {

        long saleId = insertSale(sale);

        for (SaleItem item : items) {
            item.saleId = saleId;
        }
        insertSaleItems(items);

        for (Installment installment : installments) {
            installment.saleId = saleId;
        }
        insertInstallments(installments);

        return saleId;
    }

   
    @Query("DELETE FROM sales WHERE saleId = :id")
    int deleteSale(long id);

    
    @Query("SELECT EXISTS(SELECT 1 FROM sales WHERE saleId = :id)")
    boolean existsSale(long id);

    @Transaction
    @Query("SELECT * FROM sales WHERE saleId = :id")
    SaleWithDetails getSaleWithDetailsById(long id);

    @Transaction
    @Query("SELECT * FROM sales WHERE saleId = :id")
    SaleWithProducts getSaleWithProductsById(long id);

    @Transaction
    @Query("SELECT * FROM sales ORDER BY purchase_date DESC, saleId DESC")
    List<SaleWithDetails> getAllSalesWithDetails();

    
    // As datas são gravadas como texto ISO ("2026-11-13"), que ordena corretamente como texto.
    @Transaction
    @Query("SELECT * FROM sales "
            + "WHERE (:clientId IS NULL OR client_id = :clientId) "
            + "AND (:startDate IS NULL OR purchase_date >= :startDate) "
            + "AND (:endDate IS NULL OR purchase_date <= :endDate) "
            + "ORDER BY purchase_date DESC, saleId DESC")
    List<SaleWithDetails> getSalesWithDetailsFiltered(Long clientId, LocalDate startDate, LocalDate endDate);

    @Transaction
    @Query("SELECT * FROM sales "
            + "WHERE (:clientId IS NULL OR client_id = :clientId) "
            + "AND (:startDate IS NULL OR purchase_date >= :startDate) "
            + "AND (:endDate IS NULL OR purchase_date <= :endDate) "
            + "ORDER BY purchase_date DESC, saleId DESC "
            + "LIMIT :size OFFSET :offset")
    List<SaleWithDetails> getSalesWithDetailsFilteredPage(Long clientId, LocalDate startDate, LocalDate endDate,
                                                          int size, int offset);

    
    @Transaction
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId ORDER BY saleItemId ASC")
    List<SaleItemWithProduct> getItemsBySale(long saleId);

    
    @Query("SELECT * FROM installments WHERE installmentId = :id")
    Installment getInstallmentById(long id);

    @Query("SELECT * FROM installments WHERE sale_id = :saleId ORDER BY number ASC")
    List<Installment> getInstallmentsBySale(long saleId);

    @Query("SELECT * FROM installments WHERE paid_at IS NULL AND due_date < :today ORDER BY due_date ASC")
    List<Installment> getOverdueInstallments(LocalDate today);

    @Query("SELECT * FROM installments WHERE paid_at IS NULL AND due_date BETWEEN :startDate AND :endDate ORDER BY due_date ASC")
    List<Installment> getPendingInstallmentsBetween(LocalDate startDate, LocalDate endDate);

    // A condição "paid_at IS NULL" torna a operação atômica: se duas requisições tentarem pagar
    // a mesma parcela ao mesmo tempo, só uma altera a linha (a outra recebe 0).
    @Query("UPDATE installments SET paid_at = :paidAt, paid_amount = :paidAmount "
            + "WHERE installmentId = :id AND paid_at IS NULL")
    int registerPayment(long id, LocalDate paidAt, BigDecimal paidAmount);

    @Query("UPDATE installments SET paid_at = NULL, paid_amount = NULL "
            + "WHERE installmentId = :id AND paid_at IS NOT NULL")
    int cancelPayment(long id);
}