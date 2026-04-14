package com.rvsfishworld.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProformaRecord {
    private long id;
    private String proformaNo;
    private LocalDate invoiceDate;
    private String customerCode;
    private String customerName;
    private String branchCode = "";
    private String branchName = "";
    private String salesmanCode = "";
    private String salesmanName = "";
    private java.math.BigDecimal adjustmentPercent = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal packingCharges = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal totalPayables = java.math.BigDecimal.ZERO;
    private String preparedBy = "";
    private String approvedBy = "";
    private final List<ProformaLine> lines = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProformaNo() {
        return proformaNo;
    }

    public void setProformaNo(String proformaNo) {
        this.proformaNo = proformaNo;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode == null ? "" : branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName == null ? "" : branchName;
    }

    public String getSalesmanCode() {
        return salesmanCode;
    }

    public void setSalesmanCode(String salesmanCode) {
        this.salesmanCode = salesmanCode == null ? "" : salesmanCode;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName == null ? "" : salesmanName;
    }

    public java.math.BigDecimal getAdjustmentPercent() {
        return adjustmentPercent;
    }

    public void setAdjustmentPercent(java.math.BigDecimal adjustmentPercent) {
        this.adjustmentPercent = adjustmentPercent == null ? java.math.BigDecimal.ZERO : adjustmentPercent;
    }

    public java.math.BigDecimal getPackingCharges() {
        return packingCharges;
    }

    public void setPackingCharges(java.math.BigDecimal packingCharges) {
        this.packingCharges = packingCharges == null ? java.math.BigDecimal.ZERO : packingCharges;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount == null ? java.math.BigDecimal.ZERO : totalAmount;
    }

    public java.math.BigDecimal getTotalPayables() {
        return totalPayables;
    }

    public void setTotalPayables(java.math.BigDecimal totalPayables) {
        this.totalPayables = totalPayables == null ? java.math.BigDecimal.ZERO : totalPayables;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy == null ? "" : preparedBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy == null ? "" : approvedBy;
    }

    public List<ProformaLine> getLines() {
        return lines;
    }
}
