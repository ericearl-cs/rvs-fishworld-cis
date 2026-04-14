package com.rvsfishworld.model;

public class MortalityLine {
    private long id;
    private String productCode;
    private String description;
    private String area = "";
    private int quantity;
    private java.math.BigDecimal averageCost = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal totalCost = java.math.BigDecimal.ZERO;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recompute();
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area == null ? "" : area;
    }

    public java.math.BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(java.math.BigDecimal averageCost) {
        this.averageCost = averageCost == null ? java.math.BigDecimal.ZERO : averageCost;
        recompute();
    }

    public java.math.BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(java.math.BigDecimal totalCost) {
        this.totalCost = totalCost == null ? java.math.BigDecimal.ZERO : totalCost;
    }

    public void recompute() {
        totalCost = averageCost.multiply(java.math.BigDecimal.valueOf(Math.max(0, quantity)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
