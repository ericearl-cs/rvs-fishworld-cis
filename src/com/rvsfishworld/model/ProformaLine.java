package com.rvsfishworld.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProformaLine {
    private long id;
    private int lineNo;
    private String transShipperCode = "";
    private String boxNo = "";
    private String productCode;
    private String description;
    private String supplierCode = "";
    private int quantity;
    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;

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
        this.transShipperCode = transShipperCode == null ? "" : transShipperCode;
    }

    public String getBoxNo() {
        return boxNo;
    }

    public void setBoxNo(String boxNo) {
        this.boxNo = boxNo == null ? "" : boxNo;
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
        this.supplierCode = supplierCode == null ? "" : supplierCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = money(price);
        recompute();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = money(totalPrice);
    }

    public void recompute() {
        totalPrice = money(price.multiply(BigDecimal.valueOf(Math.max(0, quantity))));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
