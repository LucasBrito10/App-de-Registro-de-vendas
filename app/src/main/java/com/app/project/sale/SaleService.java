package com.app.project.sale;


public class SaleService {

    private final SaleRepository saleRepository ;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }
    
}
