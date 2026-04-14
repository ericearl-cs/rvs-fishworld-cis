package com.rvsfishworld.ui;

public class MortalityInternalFrame extends PlaceholderInternalFrame {
    public MortalityInternalFrame() {
        super(
                "Mortality",
                "Mortality",
                new String[]{"Order by Ref No.", "Order by Date", "Order by Area"},
                new String[]{"Find", "Add", "Edit", "Delete", "Print", "Exit"},
                new String[]{"Reference No.", "Date", "Area", "Product No.", "Quantity", "Average Cost", "Total", "Dead", "Damage"},
                new Object[][]{
                        {"MOR-0001", "03/18/2026", "STATION 1", "01001", "2", "20.00", "40.00", "2", "0"},
                        {"MOR-0002", "03/18/2026", "STATION 2", "01005", "1", "55.00", "55.00", "1", "0"}
                },
                "Mortality shell is ready. Stock deduction logic is next."
        );
    }
}
