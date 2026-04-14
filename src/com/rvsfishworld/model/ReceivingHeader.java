package com.rvsfishworld.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceivingHeader {
    private long receivingId;
    private String rrNo;
    private LocalDate dateReceived;
    private boolean directPurchase;
    private boolean cancelled;
    private long supplierId;
    private String supplierCode;
    private String supplierName;
    private String ltpNo;
    private long branchId;
    private String branchCode;
    private String branchName;
    private long currencyId;
    private String currencyCode;
    private String currencyName;
    private String encodedBy;
    private String checkedBy;
    private String approvedBy;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private final List<ReceivingLine> lines = new ArrayList<>();

    public long getReceivingId() { return receivingId; }
    public void setReceivingId(long receivingId) { this.receivingId = receivingId; }

    public String getRrNo() { return rrNo; }
    public void setRrNo(String rrNo) { this.rrNo = rrNo; }

    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }

    public boolean isDirectPurchase() { return directPurchase; }
    public void setDirectPurchase(boolean directPurchase) { this.directPurchase = directPurchase; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public long getSupplierId() { return supplierId; }
    public void setSupplierId(long supplierId) { this.supplierId = supplierId; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getLtpNo() { return ltpNo; }
    public void setLtpNo(String ltpNo) { this.ltpNo = ltpNo; }

    public long getBranchId() { return branchId; }
    public void setBranchId(long branchId) { this.branchId = branchId; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public long getCurrencyId() { return currencyId; }
    public void setCurrencyId(long currencyId) { this.currencyId = currencyId; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }

    public String getEncodedBy() { return encodedBy; }
    public void setEncodedBy(String encodedBy) { this.encodedBy = encodedBy; }

    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<ReceivingLine> getLines() { return lines; }
}
