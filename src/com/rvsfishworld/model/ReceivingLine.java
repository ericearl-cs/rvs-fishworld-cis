package com.rvsfishworld.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReceivingLine {
    private int lineNo;
    private String groupCode;
    private long productId;
    private String productCode;
    private String productDescription;
    private BigDecimal qtyDelivered = BigDecimal.ZERO;
    private BigDecimal qtyDoa = BigDecimal.ZERO;
    private BigDecimal qtyRejected = BigDecimal.ZERO;
    private String rejectReason;
    private String tank;
    private BigDecimal qtyBought = BigDecimal.ZERO;
    private BigDecimal unitCost = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private boolean stopFlag;

    public void recompute() {
        if (qtyDelivered == null) qtyDelivered = BigDecimal.ZERO;
        if (qtyDoa == null) qtyDoa = BigDecimal.ZERO;
        if (qtyRejected == null) qtyRejected = BigDecimal.ZERO;
        if (unitCost == null) unitCost = BigDecimal.ZERO;

        qtyBought = qtyDelivered.subtract(qtyDoa).subtract(qtyRejected);
        if (qtyBought.compareTo(BigDecimal.ZERO) < 0) {
            qtyBought = BigDecimal.ZERO;
        }
        totalCost = qtyBought.multiply(unitCost).setScale(4, RoundingMode.HALF_UP);
    }

    public int getLineNo() { return lineNo; }
    public void setLineNo(int lineNo) { this.lineNo = lineNo; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public BigDecimal getQtyDelivered() { return qtyDelivered; }
    public void setQtyDelivered(BigDecimal qtyDelivered) { this.qtyDelivered = qtyDelivered; }

    public BigDecimal getQtyDoa() { return qtyDoa; }
    public void setQtyDoa(BigDecimal qtyDoa) { this.qtyDoa = qtyDoa; }

    public BigDecimal getQtyRejected() { return qtyRejected; }
    public void setQtyRejected(BigDecimal qtyRejected) { this.qtyRejected = qtyRejected; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getTank() { return tank; }
    public void setTank(String tank) { this.tank = tank; }

    public BigDecimal getQtyBought() { return qtyBought; }
    public void setQtyBought(BigDecimal qtyBought) { this.qtyBought = qtyBought; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public boolean isStopFlag() { return stopFlag; }
    public void setStopFlag(boolean stopFlag) { this.stopFlag = stopFlag; }
}
