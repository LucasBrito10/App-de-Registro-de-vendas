package com.app.project.sale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Camada de acesso a dados das vendas: traduz as necessidades do service em chamadas ao DAO.
public class SaleRepository {

    private final SaleDao saleDao;

    public SaleRepository(SaleDao saleDao) {
        this.saleDao = saleDao;
    }

    // ---------------------------------------------------------------------------------------
    // Vendas
    // ---------------------------------------------------------------------------------------

    // Grava venda + itens + parcelas de forma atômica. Retorna a venda completa recém-criada.
    public SaleWithDetails createSale(Sale sale, List<SaleItem> items, List<Installment> installments) {

        long saleId = saleDao.insertSaleWithDetails(sale, items, installments);

        return saleDao.getSaleWithDetailsById(saleId);
    }

    public boolean deleteSale(long id) {
        return saleDao.deleteSale(id) > 0;
    }

    public boolean existsById(long id) {
        return saleDao.existsSale(id);
    }

    public SaleWithDetails findWithDetailsById(long id) {
        return saleDao.getSaleWithDetailsById(id);
    }

    public SaleWithProducts findWithProductsById(long id) {
        return saleDao.getSaleWithProductsById(id);
    }

    public List<SaleWithDetails> findAll() {
        return saleDao.getAllSalesWithDetails();
    }

    // Parâmetros null desligam o filtro correspondente.
    public List<SaleWithDetails> findFiltered(Long clientId, LocalDate startDate, LocalDate endDate) {
        return saleDao.getSalesWithDetailsFiltered(clientId, startDate, endDate);
    }

    public List<SaleWithDetails> findFilteredPage(Long clientId, LocalDate startDate, LocalDate endDate,
                                                  int page, int size) {
        return saleDao.getSalesWithDetailsFilteredPage(clientId, startDate, endDate, size, (page - 1) * size);
    }

    // ---------------------------------------------------------------------------------------
    // Itens
    // ---------------------------------------------------------------------------------------

    public List<SaleItemWithProduct> findItemsBySale(long saleId) {
        return saleDao.getItemsBySale(saleId);
    }

    // ---------------------------------------------------------------------------------------
    // Parcelas
    // ---------------------------------------------------------------------------------------

    public Installment findInstallmentById(long id) {
        return saleDao.getInstallmentById(id);
    }

    public List<Installment> findInstallmentsBySale(long saleId) {
        return saleDao.getInstallmentsBySale(saleId);
    }

    public List<Installment> findOverdueInstallments(LocalDate today) {
        return saleDao.getOverdueInstallments(today);
    }

    public List<Installment> findPendingInstallmentsBetween(LocalDate startDate, LocalDate endDate) {
        return saleDao.getPendingInstallmentsBetween(startDate, endDate);
    }

    // Retorna false se a parcela não existir OU se já estiver paga.
    public boolean registerPayment(long installmentId, LocalDate paidAt, BigDecimal paidAmount) {
        return saleDao.registerPayment(installmentId, paidAt, paidAmount) > 0;
    }

    // Retorna false se a parcela não existir OU se não estiver paga.
    public boolean cancelPayment(long installmentId) {
        return saleDao.cancelPayment(installmentId) > 0;
    }
}
