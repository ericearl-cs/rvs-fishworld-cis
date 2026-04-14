package com.rvsfishworld.model;

import java.math.BigDecimal;

public class SalesInvoiceLine {
    private long id;
    private int lineNo;
    private String transShipperCode;
    private String boxNo;
    private String productCode;
    private String description;
    private String supplierCode;
    private int quantity;
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private boolean special;
    private String specialValue = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getTransShipperCode() {
        return transShipperCode;
    }

    public void setTransShipperCode(String transShipperCode) {
        this.transShipperCode = transShipperCode;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice == null ? BigDecimal.ZERO : sellingPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice == null ? BigDecimal.ZERO : totalPrice;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public String getSpecialValue() {
        return specialValue;
    }

    public void setSpecialValue(String specialValue) {
        this.specialValue = specialValue == null ? "" : specialValue;
    }

    public void recompute() {
        totalPrice = sellingPrice.multiply(BigDecimal.valueOf(Math.max(0, quantity)));
    }
}
