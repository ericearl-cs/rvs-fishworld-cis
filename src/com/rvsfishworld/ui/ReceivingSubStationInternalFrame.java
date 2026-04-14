package com.rvsfishworld.ui;

public class ReceivingSubStationInternalFrame extends PlaceholderInternalFrame {
    public ReceivingSubStationInternalFrame() {
        super(
                "Receiving Sub-Station",
                "Receiving Sub-Station",
                new String[]{"Order by R.S. No.", "Order by Date", "Order by Area"},
                new String[]{"Find", "Add", "Edit", "Delete", "Print", "Exit"},
                new String[]{"R.S. No.", "Date", "Area", "Product No.", "Description", "Qty", "Unit Cost", "Total"},
                new Object[][]{
                        {"RSS-0001", "03/18/2026", "STATION 1", "01001", "BANDED ANGEL (REEF SAFE)", "15", "20.00", "300.00"},
                        {"RSS-0001", "03/18/2026", "STATION 1", "01005", "BLUE BELLUS ANGEL FEMALE (REEF SAFE)", "10", "55.00", "550.00"}
                },
                "Sub-station flow shell is ready. Link to main receiving is next."
        );
    }
}
