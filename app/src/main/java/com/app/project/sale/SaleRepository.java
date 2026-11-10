package com.app.project.sale;


public class SaleRepository {


    private final SaleDao saleDao;

    public SaleRepository(SaleDao saleDao) {
        this.saleDao = saleDao;
    }
    
}
