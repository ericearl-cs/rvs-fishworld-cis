package com.rvsfishworld.ui;

public class StockReportInternalFrame extends PlaceholderInternalFrame {
    public StockReportInternalFrame() {
        super(
                "Stock Report",
                "Stock Report",
                new String[]{"Order by Product No", "Order by Description", "Order by Category"},
                new String[]{"Find", "View", "Print", "Exit"},
                new String[]{"CODE", "DESCRIPTION", "SCIENTIFIC NAME", "Category", "Qty.", "Average Cost", "Price B"},
                new Object[][]{
                        {"01001", "BANDED ANGEL (REEF SAFE)", "PARACENTROPYGE MULTIFASCIATU", "01", "0.00", "20.00", "20.00"},
                        {"01005", "BLUE BELLUS ANGEL FEMALE (REEF SAFE)", "GENICANTHUS BELLUS -FEMALE", "01", "0.00", "55.00", "55.00"},
                        {"01008", "BLUE FACE ANGEL ADULT (M/L)", "EUXIPHIPOPS XANTHOMETAPON", "01", "0.00", "45.00", "45.00"}
                },
                "Stock report shell is ready. Live stock query is next."
        );
    }
}
