package com.app.project.sale;

// Um item dentro do corpo do POST /api/v1/sales: qual produto e quantas unidades.
public class SaleItemRequestDto {

    private Long productId;
    private Integer quantity;

    // Construtor vazio exigido pelo Gson.
    public SaleItemRequestDto() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
