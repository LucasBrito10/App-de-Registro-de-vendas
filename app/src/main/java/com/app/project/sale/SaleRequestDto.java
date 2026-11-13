package com.app.project.sale;

import java.time.LocalDate;
import java.util.List;


public class SaleRequestDto {

    private Long clientId;
    private LocalDate purchaseDate;
    private Integer installmentCount;
    private List<SaleItemRequestDto> items;

    // Construtor vazio exigido pelo Gson.
    public SaleRequestDto() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(Integer installmentCount) {
        this.installmentCount = installmentCount;
    }

    public List<SaleItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequestDto> items) {
        this.items = items;
    }
}
