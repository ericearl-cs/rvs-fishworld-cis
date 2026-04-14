package com.rvsfishworld.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceRecord {
    private long id;
    private String proformaNo;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String branchCode;
    private String branchName;
    private String customerCode;
    private String customerName;
    private String salesmanCode;
    private String salesmanName;
    private String pricingCode = "B";
    private String currencyCode = "USD";
    private String currencyName = "AMERICAN DOLLAR";
    private BigDecimal exchangeRate = BigDecimal.ONE;
    private BigDecimal boxQty = BigDecimal.ZERO;
    private BigDecimal totalKgs = BigDecimal.ZERO;
    private BigDecimal fishCost = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal miscAmount = BigDecimal.ZERO;
    private BigDecimal sscAmount = BigDecimal.ZERO;
    private BigDecimal rateAmount = BigDecimal.ZERO;
    private BigDecimal rate2Amount = BigDecimal.ZERO;
    private BigDecimal vatAmount = BigDecimal.ZERO;
    private BigDecimal stampAmount = BigDecimal.ZERO;
    private BigDecimal doaAmount = BigDecimal.ZERO;
    private BigDecimal freightAmount = BigDecimal.ZERO;
    private BigDecimal packingCharges = BigDecimal.ZERO;
    private BigDecimal productSalesAmount = BigDecimal.ZERO;
    private BigDecimal totalPayables = BigDecimal.ZERO;
    private BigDecimal lineAmount = BigDecimal.ZERO;
    private String sourceProformaNo;
    private long sourceProformaId;
    private String awbNo = "";
    private String broker = "";
    private boolean consumables;
    private String preparedBy = "";
    private String checkedBy = "";
    private String approvedByName = "";
    private String receivedBy = "";
    private boolean applyFormula;
    private final List<SalesInvoiceLine> lines = new ArrayList<>();

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

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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

    public String getSalesmanCode() {
        return salesmanCode;
    }

    public void setSalesmanCode(String salesmanCode) {
        this.salesmanCode = salesmanCode;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public String getPricingCode() {
        return pricingCode;
    }

    public void setPricingCode(String pricingCode) {
        this.pricingCode = pricingCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate == null ? BigDecimal.ONE : exchangeRate;
    }

    public BigDecimal getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(BigDecimal boxQty) {
        this.boxQty = zero(boxQty);
    }

    public BigDecimal getTotalKgs() {
        return totalKgs;
    }

    public void setTotalKgs(BigDecimal totalKgs) {
        this.totalKgs = zero(totalKgs);
    }

    public BigDecimal getFishCost() {
        return fishCost;
    }

    public void setFishCost(BigDecimal fishCost) {
        this.fishCost = zero(fishCost);
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = zero(discountAmount);
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = zero(discountPercent);
    }

    public BigDecimal getMiscAmount() {
        return miscAmount;
    }

    public void setMiscAmount(BigDecimal miscAmount) {
        this.miscAmount = zero(miscAmount);
    }

    public BigDecimal getSscAmount() {
        return sscAmount;
    }

    public void setSscAmount(BigDecimal sscAmount) {
        this.sscAmount = zero(sscAmount);
    }

    public BigDecimal getRateAmount() {
        return rateAmount;
    }

    public void setRateAmount(BigDecimal rateAmount) {
        this.rateAmount = zero(rateAmount);
    }

    public BigDecimal getRate2Amount() {
        return rate2Amount;
    }

    public void setRate2Amount(BigDecimal rate2Amount) {
        this.rate2Amount = zero(rate2Amount);
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = zero(vatAmount);
    }

    public BigDecimal getStampAmount() {
        return stampAmount;
    }

    public void setStampAmount(BigDecimal stampAmount) {
        this.stampAmount = zero(stampAmount);
    }

    public BigDecimal getDoaAmount() {
        return doaAmount;
    }

    public void setDoaAmount(BigDecimal doaAmount) {
        this.doaAmount = zero(doaAmount);
    }

    public BigDecimal getFreightAmount() {
        return freightAmount;
    }

    public void setFreightAmount(BigDecimal freightAmount) {
        this.freightAmount = zero(freightAmount);
    }

    public BigDecimal getPackingCharges() {
        return packingCharges;
    }

    public void setPackingCharges(BigDecimal packingCharges) {
        this.packingCharges = zero(packingCharges);
    }

    public BigDecimal getProductSalesAmount() {
        return productSalesAmount;
    }

    public void setProductSalesAmount(BigDecimal productSalesAmount) {
        this.productSalesAmount = zero(productSalesAmount);
    }

    public BigDecimal getTotalPayables() {
        return totalPayables;
    }

    public void setTotalPayables(BigDecimal totalPayables) {
        this.totalPayables = zero(totalPayables);
    }

    public BigDecimal getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(BigDecimal lineAmount) {
        this.lineAmount = zero(lineAmount);
    }

    public String getSourceProformaNo() {
        return sourceProformaNo;
    }

    public void setSourceProformaNo(String sourceProformaNo) {
        this.sourceProformaNo = sourceProformaNo;
    }

    public long getSourceProformaId() {
        return sourceProformaId;
    }

    public void setSourceProformaId(long sourceProformaId) {
        this.sourceProformaId = sourceProformaId;
    }

    public String getAwbNo() {
        return awbNo;
    }

    public void setAwbNo(String awbNo) {
        this.awbNo = awbNo == null ? "" : awbNo;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker == null ? "" : broker;
    }

    public boolean isApplyFormula() {
        return applyFormula;
    }

    public void setApplyFormula(boolean applyFormula) {
        this.applyFormula = applyFormula;
    }

    public boolean isConsumables() {
        return consumables;
    }

    public void setConsumables(boolean consumables) {
        this.consumables = consumables;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy == null ? "" : preparedBy;
    }

    public String getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(String checkedBy) {
        this.checkedBy = checkedBy == null ? "" : checkedBy;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName == null ? "" : approvedByName;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy == null ? "" : receivedBy;
    }

    public List<SalesInvoiceLine> getLines() {
        return lines;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
