package com.rvsfishworld.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DisbursementHeader {
    private long id;
    private String cvNo;
    private LocalDate cvDate;
    private String supplierCode;
    private String supplierName;
    private BigDecimal amount = BigDecimal.ZERO;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCvNo() {
        return cvNo;
    }

    public void setCvNo(String cvNo) {
        this.cvNo = cvNo;
    }

    public LocalDate getCvDate() {
        return cvDate;
    }

    public void setCvDate(LocalDate cvDate) {
        this.cvDate = cvDate;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }
}
