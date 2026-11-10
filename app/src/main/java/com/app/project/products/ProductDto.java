package com.app.project.products;

import java.math.BigDecimal;

public class ProductDto {
    
    private long id;
    private String name;
    private BigDecimal price;

    // Construtor vazio exigido por bibliotecas de serialização como o Gson
    public ProductDto() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}