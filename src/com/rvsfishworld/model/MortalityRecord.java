package com.rvsfishworld.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MortalityRecord {
    private long id;
    private String referenceNo;
    private LocalDate recordDate;
    private String area = "";
    private java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
    private final List<MortalityLine> lines = new ArrayList<>();

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

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area == null ? "" : area;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount == null ? java.math.BigDecimal.ZERO : totalAmount;
    }

    public List<MortalityLine> getLines() {
        return lines;
    }
}
