package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.model.MortalityLine;
import com.rvsfishworld.model.MortalityRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.Window;
import java.text.DecimalFormat;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class MortalityPrintDialog extends FoxProChildDialog {
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    public MortalityPrintDialog(Window owner, MortalityRecord record) {
        super(owner, "Mortality Print Preview", 760, 560);
        setContentPane(buildContent(record));
    }

    private JScrollPane buildContent(MortalityRecord record) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(FoxProTheme.FONT);
        area.setText(preview(record));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        return scroll;
    }

    private String preview(MortalityRecord record) {
        if (record == null) {
            return "No mortality record selected.";
        }
        StringBuilder text = new StringBuilder();
        text.append("MORTALITY").append(System.lineSeparator());
        text.append("Reference No: ").append(blank(record.getReferenceNo())).append(System.lineSeparator());
        text.append("Date: ").append(record.getRecordDate() == null ? "" : record.getRecordDate()).append(System.lineSeparator());
        text.append("Area: ").append(blank(record.getArea())).append(System.lineSeparator());
        text.append(System.lineSeparator());
        text.append(String.format("%-12s %-38s %8s %12s %12s%n", "PRODUCT", "DESCRIPTION", "QTY", "AVG COST", "TOTAL"));
        text.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        for (MortalityLine line : record.getLines()) {
            text.append(String.format(
                    "%-12s %-38.38s %8d %12s %12s%n",
                    blank(line.getProductCode()),
                    blank(line.getDescription()),
                    Math.max(0, line.getQuantity()),
                    MONEY.format(line.getAverageCost()),
                    MONEY.format(line.getTotalCost())));
        }
        text.append(System.lineSeparator());
        text.append("TOTAL: ").append(MONEY.format(record.getTotalAmount())).append(System.lineSeparator());
        return text.toString();
    }

    private String blank(String value) {
        return value == null ? "" : value;
    }
}
