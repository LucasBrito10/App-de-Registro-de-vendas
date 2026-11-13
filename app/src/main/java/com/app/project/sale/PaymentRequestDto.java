package com.app.project.sale;

import java.math.BigDecimal;
import java.time.LocalDate;


public class PaymentRequestDto {

    private LocalDate paidAt;
    private BigDecimal paidAmount;

    // Construtor vazio exigido pelo Gson (e usado quando a requisição vem sem corpo).
    public PaymentRequestDto() {
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDate paidAt) {
        this.paidAt = paidAt;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
}
