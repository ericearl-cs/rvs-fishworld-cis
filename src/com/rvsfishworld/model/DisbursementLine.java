package com.rvsfishworld.model;

import java.math.BigDecimal;

public class DisbursementLine {
    private long id;
    private String referenceNo;
    private BigDecimal amount = BigDecimal.ZERO;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }
}
